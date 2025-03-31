package com.lvmaoya.blog.domain.vo;

import java.io.Serializable;

// 通用返回类
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code; // 状态码
    private String message; // 消息
    private T data; // 数据

    // 成功返回，带数据
    public static <T> R<T> success(T data) {
        return new R<>(200, "操作成功", data);
    }

    // 成功返回，不带数据
    public static <T> R<T> success() {
        return new R<>(200, "操作成功", null);
    }

    // 失败返回
    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null);
    }

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}