package com.lvmaoya.blog.handler.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}