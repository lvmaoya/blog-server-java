package com.lvmaoya.blog.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lvmaoya.blog.domain.vo.R;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatController {

    @Resource
    private OpenAiChatModel chatModel;

    private final List<Message> chatHistoryList = new ArrayList<>();

    @GetMapping("/chat")
    public R chat(String message) {
        // 验证输入
        if (StringUtils.isBlank(message)) {
            throw new IllegalCallerException("消息内容不能为空");
        }
        try {
            // 创建只包含当前消息的Prompt
            Prompt prompt = new Prompt(new UserMessage(message));

            // 获取AI响应
            ChatResponse chatResponse = chatModel.call(prompt);

            return R.success(chatResponse.getResult().getOutput().getContent());

        } catch (Exception e) {
            return R.error(2000, "处理请求时发生错误: " + e.getMessage());
        }
    }

}

