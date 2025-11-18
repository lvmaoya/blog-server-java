package com.lvmaoya.blog.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 会话历史中的一条消息记录，用于持久化存储到 Redis。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色：system / user / assistant
     */
    private String role;

    /**
     * 文本内容
     */
    private String content;
}