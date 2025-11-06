package com.lvmaoya.blog.h5Controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lvmaoya.blog.domain.dto.ChatBotRequest;
import com.lvmaoya.blog.domain.vo.ChatBotResponse;
import com.lvmaoya.blog.domain.dto.ChatMessageRecord;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.rag.RagVectorIndexService;
import com.lvmaoya.blog.service.rag.RagVectorSearchService;
import com.lvmaoya.blog.service.rag.RagVectorSearchService.SearchHit;
import com.lvmaoya.blog.utils.RedisCacheUtil;
import jakarta.annotation.Resource;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import com.lvmaoya.blog.service.rag.ZhipuEmbeddingService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * H5 前端 Chat-Bot 接口，支持多轮对话。
 * DeepSeek API 为无状态接口，每次调用需要携带完整历史。
 * 这里通过 chatId 在内存中维护会话历史，保证多轮上下文。
 */
@RestController
@RequestMapping("/h5")
public class H5ChatController {

    private static final Logger log = LoggerFactory.getLogger(H5ChatController.class);

    @Resource
    private OpenAiChatModel chatModel;
    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private RagVectorSearchService ragVectorSearchService;
    @Resource
    private RagVectorIndexService ragVectorIndexService;
    @Resource
    private MilvusClientV2 milvusClient;
    @Resource
    private ZhipuEmbeddingService zhipuEmbeddingService;
    @Resource
    private Environment env;

    // Redis key 前缀与过期时间（秒）
    private static final String CHAT_HISTORY_PREFIX = "chat:history:";
    private static final long CHAT_TTL_SECONDS = 3 * 24 * 3600; // 3 天
    // 携带到模型的历史条数上限（不含系统消息）
    private static final int MAX_CARRIED_HISTORY = 10;
    // RAG 命中分数阈值与链接数量上限
    private static final float MIN_RAG_SCORE = 0.55f;
    // 仅在高相关命中时追加链接的阈值（更严格）
    private static final float MIN_LINK_SCORE = 0.70f;
    private static final int MAX_LINKS = 3;

    private String siteBaseUrl() {
        String url = env.getProperty("site.base-url");
        return (url == null || url.isBlank()) ? "https://lvmaoya.cn" : url;
    }

    /**
     * 语义检索增强的非流式对话。
     * 路径：POST /h5/chat/rag，Body: { chatId?, message }
     */
    @PostMapping("/chat/rag")
    public R<ChatBotResponse> chatWithRag(@RequestBody ChatBotRequest request) {
        if (request == null || StringUtils.isBlank(request.getMessage())) {
            return R.error(400, "消息内容不能为空");
        }

        String chatId = StringUtils.isBlank(request.getChatId())
                ? UUID.randomUUID().toString()
                : request.getChatId();

        String key = CHAT_HISTORY_PREFIX + chatId;
        Object cached = redisCacheUtil.get(key);
        List<ChatMessageRecord> records;
        if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ChatMessageRecord) {
            //noinspection unchecked
            records = (List<ChatMessageRecord>) cached;
        } else {
            records = new ArrayList<>();
        }

        // 语义检索构建上下文（寒暄/泛问跳过检索）
        List<SearchHit> hits = isSmallTalk(request.getMessage())
                ? java.util.Collections.emptyList()
                : ragVectorSearchService.searchBySemantic(request.getMessage(), null, null, null);
        // 过滤低分命中，若无有效资料则直接拒答（不幽默）
        List<SearchHit> validHits = new java.util.ArrayList<>();
        if (hits != null) {
            for (SearchHit h : hits) {
                if (h != null && h.score != null && h.score >= MIN_RAG_SCORE) {
                    validHits.add(h);
                }
            }
        }
        String context = buildContextFromHits(validHits);

        // 携带系统消息 + 最近历史 + 语义上下文
        List<ChatMessageRecord> carriedRecords = limitHistory(records, MAX_CARRIED_HISTORY);
        List<Message> history = new ArrayList<>();
        history.add(new SystemMessage(assistantPersonaPrompt(context)));
        for (ChatMessageRecord r : carriedRecords) {
            switch (r.getRole()) {
                case "user" -> history.add(new UserMessage(r.getContent()));
                case "assistant" -> history.add(new AssistantMessage(r.getContent()));
            }
        }
        history.add(new UserMessage(request.getMessage()));
        records.add(new ChatMessageRecord("user", request.getMessage()));
        // 打印完整 Prompt 历史（系统 + 历史对话 + 当前用户消息）
        logPromptHistory(history);

        try {
            ChatResponse chatResponse = chatModel.call(new Prompt(history));
            String answer = chatResponse.getResult().getOutput().getContent();
            history.add(new AssistantMessage(answer));
            records.add(new ChatMessageRecord("assistant", answer));
            List<ChatMessageRecord> trimmed = limitHistory(records, MAX_CARRIED_HISTORY);
            redisCacheUtil.set(key, trimmed, CHAT_TTL_SECONDS);
            return R.success(new ChatBotResponse(chatId, answer));
        } catch (Exception e) {
            return R.error(2000, "处理请求时发生错误: " + e.getMessage());
        }
    }

    /**
     * 语义检索增强的流式对话（SSE）。
     * 路径：POST /h5/chat/rag/stream，Body: { chatId, message }
     */
    @PostMapping("/chat/rag/stream")
    public SseEmitter chatStreamWithRag(@RequestBody ChatBotRequest request) {
        if (request == null || StringUtils.isBlank(request.getMessage())) {
            SseEmitter bad = new SseEmitter(0L);
            try { bad.send(SseEmitter.event().name("error").data("消息内容不能为空")); } catch (Exception ignored) {}
            bad.complete();
            return bad;
        }
        if (StringUtils.isBlank(request.getChatId())) {
            SseEmitter bad = new SseEmitter(0L);
            try { bad.send(SseEmitter.event().name("error").data("chatId不能为空")); } catch (Exception ignored) {}
            bad.complete();
            return bad;
        }

        String id = request.getChatId();
        String key = CHAT_HISTORY_PREFIX + id;
        Object cached = redisCacheUtil.get(key);
        List<ChatMessageRecord> records;
        if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ChatMessageRecord) {
            //noinspection unchecked
            records = (List<ChatMessageRecord>) cached;
        } else {
            records = new ArrayList<>();
        }

        // 语义检索构建上下文（寒暄/泛问跳过检索）
        List<SearchHit> hits = isSmallTalk(request.getMessage())
                ? java.util.Collections.emptyList()
                : ragVectorSearchService.searchBySemantic(request.getMessage(), null, null, null);
        List<SearchHit> validHits = new java.util.ArrayList<>();
        if (hits != null) {
            for (SearchHit h : hits) {
                if (h != null && h.score != null && h.score >= MIN_RAG_SCORE) {
                    validHits.add(h);
                }
            }
        }
        String context = buildContextFromHits(validHits);

        List<ChatMessageRecord> carriedRecords = limitHistory(records, MAX_CARRIED_HISTORY);
        List<Message> history = new ArrayList<>();
        history.add(new SystemMessage(assistantPersonaPrompt(context)));
        for (ChatMessageRecord r : carriedRecords) {
            switch (r.getRole()) {
                case "user" -> history.add(new UserMessage(r.getContent()));
                case "assistant" -> history.add(new AssistantMessage(r.getContent()));
            }
        }
        history.add(new UserMessage(request.getMessage()));
        records.add(new ChatMessageRecord("user", request.getMessage()));
        // 打印完整 Prompt 历史（系统 + 历史对话 + 当前用户消息）
        logPromptHistory(history);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        StringBuilder answerBuilder = new StringBuilder();

        try {
            chatModel.stream(new Prompt(history)).subscribe(
                    chunk -> {
                        String delta = chunk.getResult().getOutput().getContent();
                        if (delta != null && !delta.isEmpty()) {
                            answerBuilder.append(delta);
                            try {
                                emitter.send(SseEmitter.event().name("message").data(delta));
                            } catch (Exception ignored) {}
                        }
                    },
                    error -> {
                        try {
                            emitter.send(SseEmitter.event().name("error").data("处理请求时发生错误: " + error.getMessage()));
                        } catch (Exception ignored) {}
                        emitter.complete();
                    },
                    () -> {
                        String fullAnswer = answerBuilder.toString();
                        history.add(new AssistantMessage(fullAnswer));
                        records.add(new ChatMessageRecord("assistant", fullAnswer));
                        List<ChatMessageRecord> trimmed = limitHistory(records, MAX_CARRIED_HISTORY);
                        redisCacheUtil.set(key, trimmed, CHAT_TTL_SECONDS);
                        try {
                            emitter.send(SseEmitter.event().name("end").data(id));
                        } catch (Exception ignored) {}
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("处理请求时发生错误: " + e.getMessage()));
            } catch (Exception ignored) {}
            emitter.complete();
        }

        return emitter;
    }

    /**
     * 简单寒暄/非站内问题检测，减少无意义检索
     */
    private static boolean isSmallTalk(String msg) {
        if (msg == null) return true;
        String m = msg.trim().toLowerCase();
        if (m.length() <= 3) return true; // 极短文本多为寒暄
        String[] patterns = new String[] {
                "hi", "hello", "who are you", "hey",
                "你是谁", "你好", "嗨", "在吗", "哈喽", "你好啊",
                "早上好", "晚上好", "中午好", "您好"
        };
        for (String p : patterns) {
            if (m.contains(p)) return true;
        }
        return false;
    }

    private String buildContextFromHits(List<SearchHit> hits) {
        if (hits == null || hits.isEmpty()) return "(未检索到相关资料)";
        String base = siteBaseUrl();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            SearchHit h = hits.get(i);
            sb.append("[片段").append(i + 1).append("] 标题: ")
              .append(h.title == null ? "" : h.title)
              .append("\n内容预览: ")
              .append(h.contentPreview == null ? "" : h.contentPreview)
              .append("\n文章ID: ")
              .append(h.blogId == null ? "" : String.valueOf(h.blogId))
              .append("\n链接: ")
              .append(base)
              .append("/detail/")
              .append(h.blogId == null ? "" : String.valueOf(h.blogId))
              .append("\n——\n");
        }
        return sb.toString();
    }

    /**
     * 裁剪历史：保留所有系统消息，且仅保留最近 max 条非系统消息。
     */
    private static List<ChatMessageRecord> limitHistory(List<ChatMessageRecord> records, int max) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<ChatMessageRecord> systemRecords = new ArrayList<>();
        List<ChatMessageRecord> convoRecords = new ArrayList<>();
        for (ChatMessageRecord r : records) {
            if ("system".equals(r.getRole())) {
                systemRecords.add(r);
            } else {
                convoRecords.add(r);
            }
        }
        int start = Math.max(convoRecords.size() - Math.max(max, 0), 0);
        List<ChatMessageRecord> result = new ArrayList<>(systemRecords.size() + (convoRecords.size() - start));
        result.addAll(systemRecords);
        result.addAll(convoRecords.subList(start, convoRecords.size()));
        return result;
    }

    /**
     * 构建助手人设系统提示，注入 RAG 上下文
     */
    private String assistantPersonaPrompt(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 lvmaoya 的智能助手。\n")
          .append("【网站简介】\n")
          .append("这是一个个人博客与作品展示网站，内容涉及：\n")
          .append("- Web 开发、AI、数据分析、设计相关的原创内容；\n")
          .append("- 个人项目、学习总结与技术实践；\n")
          .append("- 部分文章包含代码示例与项目案例。\n\n")
          .append("【你的任务】\n")
          .append("1. 回答访客关于网站内容、文章、项目、技术栈等问题；\n")
          .append("2. 如果用户询问作者本人，请简要介绍“lvmaoya 是网站作者，一名热爱技术与创作的开发者”；\n")
          .append("3. 如果问题涉及网站导航（如“作品在哪里？”、“怎么联系你？”），请引导用户前往网站的相关页面；\n")
          .append("4. 如果问题与你提供的内容无关，请友好地说明“我目前只了解网站内的内容”；\n")
          .append("5. 保持回答语气：友好、简洁、有帮助；\n")
          .append("6. 不要编造不存在的页面或内容；\n")
          .append("7. 如果合适，可以推荐相关文章或作品板块。\n\n")
          .append("【回答风格】\n")
          .append("- 使用中文回答；\n")
          .append("- 语气自然、简洁；\n")
          .append("- 尽量用第一人称口吻（如“我的博客中有…”）；\n")
          .append("- 当无法确定时，请回复：“这个问题我暂时没有相关内容，不过你可以浏览博客主页看看是否有相关主题。”\n\n")
          .append("【可用导航提示】\n")
          .append("- 博客文章 → https://lvmaoya.cn/blog\n")
          .append("- 作品展示 → https://lvmaoya.cn/work\n")
          .append("- 关于我 → https://lvmaoya.cn/about\n")
          .append("- 联系方式 → mailto:1504734652@qq.com\n\n")
          .append("严格依据下方资料作答，若资料不足请直接说明无法回答，不要编造。以下是相关资料：\n")
          .append(context == null || context.isBlank() ? "(未检索到相关资料)" : context);
        return sb.toString();
    }

    /**
     * 调试辅助：打印当前请求携带的完整 Prompt 历史（系统/用户/助手）。
     */
    private void logPromptHistory(List<Message> history) {
        if (history == null || history.isEmpty()) {
            log.info("携带的 Prompt 历史为空");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("携带的 Prompt 历史：\n");
        int idx = 1;
        for (Message m : history) {
            try {
                sb.append(idx++)
                  .append(". ")
                  .append(m.getMessageType())
                  .append(": ")
                  .append(m.getContent())
                  .append("\n");
            } catch (Exception e) {
                sb.append(idx++)
                  .append(". ")
                  .append("<消息打印失败: ")
                  .append(e.getMessage())
                  .append(">\n");
            }
        }
        log.info(sb.toString());
    }
}
