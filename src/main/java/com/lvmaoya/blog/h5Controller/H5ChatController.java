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
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
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

    @Resource
    private OpenAiChatModel chatModel;
    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private RagVectorSearchService ragVectorSearchService;
    @Resource
    private RagVectorIndexService ragVectorIndexService;

    // Redis key 前缀与过期时间（秒）
    private static final String CHAT_HISTORY_PREFIX = "chat:history:";
    private static final long CHAT_TTL_SECONDS = 3 * 24 * 3600; // 3 天
    // 携带到模型的历史条数上限（不含系统消息）
    private static final int MAX_CARRIED_HISTORY = 10;

    @PostMapping("/chat")
    public R<ChatBotResponse> chat(@RequestBody ChatBotRequest request) {
        if (request == null || StringUtils.isBlank(request.getMessage())) {
            return R.error(400, "消息内容不能为空");
        }

        // 如果没有传入 chatId，则创建新的会话
        String chatId = StringUtils.isBlank(request.getChatId())
                ? UUID.randomUUID().toString()
                : request.getChatId();

        // 读取或初始化历史（持久化在 Redis）
        String key = CHAT_HISTORY_PREFIX + chatId;
        Object cached = redisCacheUtil.get(key);
        List<ChatMessageRecord> records;
        if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ChatMessageRecord) {
            //noinspection unchecked
            records = (List<ChatMessageRecord>) cached;
        } else {
            records = new ArrayList<>();
            records.add(new ChatMessageRecord("system", "你是一位乐于助人的助手"));
        }

        // 仅携带系统消息 + 最近 MAX_CARRIED_HISTORY 条对话消息
        List<ChatMessageRecord> carriedRecords = limitHistory(records, MAX_CARRIED_HISTORY);
        List<Message> history = new ArrayList<>();
        for (ChatMessageRecord r : carriedRecords) {
            switch (r.getRole()) {
                case "system" -> history.add(new SystemMessage(r.getContent()));
                case "user" -> history.add(new UserMessage(r.getContent()));
                case "assistant" -> history.add(new AssistantMessage(r.getContent()));
            }
        }

        // 追加用户消息
        history.add(new UserMessage(request.getMessage()));
        records.add(new ChatMessageRecord("user", request.getMessage()));

        try {
            // 传入完整历史，调用模型
            ChatResponse chatResponse = chatModel.call(new Prompt(history));
            String answer = chatResponse.getResult().getOutput().getContent();

            // 记录助手回复到历史并持久化（裁剪至上限）
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
            records.add(new ChatMessageRecord("system", "你是一位乐于助人的助手"));
        }

        // 语义检索构建上下文
        List<SearchHit> hits = ragVectorSearchService.searchBySemantic(request.getMessage(), null, null, null);
        String context = buildContextFromHits(hits);

        // 携带系统消息 + 最近历史 + 语义上下文
        List<ChatMessageRecord> carriedRecords = limitHistory(records, MAX_CARRIED_HISTORY);
        List<Message> history = new ArrayList<>();
        history.add(new SystemMessage("你是一个支持基于知识库检索的助手。以下是相关资料：\n" + context));
        for (ChatMessageRecord r : carriedRecords) {
            switch (r.getRole()) {
                case "system" -> history.add(new SystemMessage(r.getContent()));
                case "user" -> history.add(new UserMessage(r.getContent()));
                case "assistant" -> history.add(new AssistantMessage(r.getContent()));
            }
        }
        history.add(new UserMessage(request.getMessage()));
        records.add(new ChatMessageRecord("user", request.getMessage()));

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
     * 流式输出（SSE）。使用 POST 请求体，便于前端通过 fetch 处理长文本。
     * 路径：POST /h5/chat/stream，Body: { chatId?, message }
     */
    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@RequestBody ChatBotRequest request) {
        if (request == null || StringUtils.isBlank(request.getMessage())) {
            // 直接返回一个已完成的 emitter，避免 500
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

        // 读取或初始化历史（持久化在 Redis）
        String key = CHAT_HISTORY_PREFIX + id;
        Object cached = redisCacheUtil.get(key);
        List<ChatMessageRecord> records;
        if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ChatMessageRecord) {
            //noinspection unchecked
            records = (List<ChatMessageRecord>) cached;
        } else {
            records = new ArrayList<>();
            records.add(new ChatMessageRecord("system", "你是一位乐于助人的助手"));
        }

        // 仅携带系统消息 + 最近 MAX_CARRIED_HISTORY 条对话消息
        List<ChatMessageRecord> carriedRecords = limitHistory(records, MAX_CARRIED_HISTORY);
        List<Message> history = new ArrayList<>();
        for (ChatMessageRecord r : carriedRecords) {
            switch (r.getRole()) {
                case "system" -> history.add(new SystemMessage(r.getContent()));
                case "user" -> history.add(new UserMessage(r.getContent()));
                case "assistant" -> history.add(new AssistantMessage(r.getContent()));
            }
        }

        // 追加本轮用户消息
        history.add(new UserMessage(request.getMessage()));
        records.add(new ChatMessageRecord("user", request.getMessage()));

        // 设置较长超时，避免连接被过早关闭
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
                        // 流结束后，把完整回答写入历史并持久化（裁剪至上限）
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
            records.add(new ChatMessageRecord("system", "你是一位乐于助人的助手"));
        }

        // 语义检索构建上下文
        List<SearchHit> hits = ragVectorSearchService.searchBySemantic(request.getMessage(), null, null, null);
        String context = buildContextFromHits(hits);

        List<ChatMessageRecord> carriedRecords = limitHistory(records, MAX_CARRIED_HISTORY);
        List<Message> history = new ArrayList<>();
        history.add(new SystemMessage("你是一个支持基于知识库检索的助手。以下是相关资料：\n" + context));
        for (ChatMessageRecord r : carriedRecords) {
            switch (r.getRole()) {
                case "system" -> history.add(new SystemMessage(r.getContent()));
                case "user" -> history.add(new UserMessage(r.getContent()));
                case "assistant" -> history.add(new AssistantMessage(r.getContent()));
            }
        }
        history.add(new UserMessage(request.getMessage()));
        records.add(new ChatMessageRecord("user", request.getMessage()));

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
     * 触发向量索引重建（全量）。
     * 路径：POST /h5/rag/reindex
     */
    @PostMapping("/rag/reindex")
    public R reindex() {
        try {
            ragVectorIndexService.rebuildVectorIndex();
            return R.success("重建完成");
        } catch (Exception e) {
            return R.error(2000, "重建失败: " + e.getMessage());
        }
    }

    private String buildContextFromHits(List<SearchHit> hits) {
        if (hits == null || hits.isEmpty()) return "(未检索到相关资料)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            SearchHit h = hits.get(i);
            sb.append("[片段").append(i + 1).append("] 标题: ")
                    .append(h.title == null ? "" : h.title)
                    .append("\n内容预览: ")
                    .append(h.contentPreview == null ? "" : h.contentPreview)
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
}
