package com.lvmaoya.blog.blog.service;

import com.lvmaoya.blog.blog.entity.Blog;
import com.lvmaoya.blog.blog.entity.BlogContent;
import com.lvmaoya.blog.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.blog.mapper.BlogMapper;
import com.lvmaoya.blog.chat.service.RagVectorIndexService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AsyncBlogService {
    private static final Logger log = LoggerFactory.getLogger(AsyncBlogService.class);

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private BlogContentMapper blogContentMapper;
    @Resource
    private OpenAiChatModel chatModel;
    @Resource
    private RagVectorIndexService ragVectorIndexService;

    @Async("taskExecutor")
    public void updateBlog(Integer articleId) {
        long start = System.currentTimeMillis();
        try {
            Blog blog = blogMapper.selectById(articleId);
            BlogContent blogContent = blogContentMapper.selectById(articleId);
            if (blog == null) {
                log.warn("摘要生成取消：博客不存在，articleId={}", articleId);
                return;
            }
            String title = blog.getTitle() == null ? "" : blog.getTitle();
            String description = blog.getDescription() == null ? "" : blog.getDescription();
            String content = blogContent != null && blogContent.getContent() != null ? blogContent.getContent() : "";

            // 内容截断，避免超过模型或网关限制
            int maxLen = 3000;
            String truncated = content.length() > maxLen ? content.substring(0, maxLen) : content;

            String abstractText = generateAbstract(title, description, truncated);
            if (abstractText == null || abstractText.isBlank()) {
                // 兜底：使用描述或正文前150字
                String fallback = !description.isBlank() ? description : (truncated.length() > 150 ? truncated.substring(0, 150) : truncated);
                abstractText = fallback;
                log.warn("摘要生成为空，使用兜底内容，articleId={}", articleId);
            }

            blog.setArticleAbstract(abstractText);
            blogMapper.updateById(blog);
            long cost = System.currentTimeMillis() - start;
            log.info("摘要已更新，articleId={}，摘要长度={}，耗时={}ms", articleId, abstractText.length(), cost);
        } catch (Exception e) {
            log.error("摘要生成失败，articleId={}", articleId, e);
        }
    }

    @Async("taskExecutor")
    public void upsertRagIndex(Long blogId) {
        try {
            ragVectorIndexService.upsertBlog(blogId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private String generateAbstract(String title, String description, String content) {
        String promptText = String.format(
                "请为以下文章生成一个150字内的简洁摘要（不要生成字数统计、不要输出总字数）：\n" +
                        "标题: %s\n" +
                        "描述: %s\n" +
                        "内容片段: %s\n" +
                        "摘要:", title, description, content);
        try {
            Prompt prompt = new Prompt(new UserMessage(promptText));
            ChatResponse response = chatModel.call(prompt);
            return response.getResult().getOutput().getContent();
        } catch (Exception e) {
            log.error("调用模型生成摘要失败", e);
            return null;
        }
    }
}
