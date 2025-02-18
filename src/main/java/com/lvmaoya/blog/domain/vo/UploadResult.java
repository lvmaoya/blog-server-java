package com.lvmaoya.blog.domain.vo;

public class UploadResult {
    private int code;
    private String message;
    private String url;

    public UploadResult(int code, String message, String url) {
        this.code = code;
        this.message = message;
        this.url = url;
    }

    // Getters 和 Setters
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}