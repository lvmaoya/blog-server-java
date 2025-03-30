package com.lvmaoya.blog.enums;

import lombok.Getter;

@Getter
public enum ArticleEnum {
    // 任务状态，值为0
    TODO(0),
    // 草稿状态，值为 0
    DRAFT(1),
    // 已发布状态，值为 2
    PUBLISHED(2),
    // 已删除状态，值为 3
    DELETED(3);

    private final int value;

    // 枚举的构造函数，用于初始化状态值
    ArticleEnum(int value) {
        this.value = value;
    }

    // 获取状态值的方法
    public int getValue() {
        return value;
    }

}
