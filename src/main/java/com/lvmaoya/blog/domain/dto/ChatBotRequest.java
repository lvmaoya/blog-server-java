package com.lvmaoya.blog.domain.dto;

import lombok.Data;

/**
 * 前端 Chat-Bot 请求体
 */
@Data
public class ChatBotRequest {
    /**
     * 会话标识，可为空。为空时后端将创建新的会话。
     */
    private String chatId;

    /**
     * 用户输入的消息，不能为空。
     */
    private String message;
}