package com.lvmaoya.blog.enums;

import lombok.Getter;

@Getter
public enum StatusCodeEnum {

    SUCCESS(200, "操作成功"),
    FAIL(500, "服务器内部错误"),
    NOT_FOUND(404, "资源未找到"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问");

    private final int code;
    private final String msg;

    StatusCodeEnum(int code, String message) {
        this.code = code;
        this.msg = message;
    }
}