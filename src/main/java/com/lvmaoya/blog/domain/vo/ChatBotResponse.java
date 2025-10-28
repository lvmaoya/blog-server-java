package com.lvmaoya.blog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端 Chat-Bot 返回体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotResponse {
    /**
     * 会话标识
     */
    private String chatId;

    /**
     * AI 回复内容
     */
    private String answer;
}