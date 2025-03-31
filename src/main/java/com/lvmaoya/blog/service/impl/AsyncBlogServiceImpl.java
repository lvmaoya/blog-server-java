package com.lvmaoya.blog.service.impl;

import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.BlogContent;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.service.AsyncBlogService;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncBlogServiceImpl implements AsyncBlogService {

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private BlogContentMapper blogContentMapper;
    @Resource
    private OpenAiChatModel chatModel;
    @Override
    @Async("taskExecutor")
    public void updateBlog(String articleId) {
        try {
            Blog blog = blogMapper.selectById(articleId);
            BlogContent blogContent = blogContentMapper.selectById(articleId);

            String abstractText = generateAbstract(
                    blog.getTitle(),
                    blog.getDescription(),
                    blogContent.getContent()
            );
            System.out.println(abstractText);

            // 更新摘要和状态
            blog.setArticleAbstract(abstractText);
            blogMapper.updateById(blog);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private String generateAbstract(String title, String description, String content) {
        String promptText = String.format(
                "请为以下文章生成一个简洁的摘要(不超过150字):\n" +
                        "标题: %s\n" +
                        "描述: %s\n" +
                        "内容: %s\n" +
                        "摘要:", title, description, content);

        Prompt prompt = new Prompt(new UserMessage(promptText));
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }
}
