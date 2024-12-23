package com.lvmaoya.blog.domain;

import lombok.Data;

import static com.lvmaoya.blog.enums.StatusCodeEnum.FAIL;
import static com.lvmaoya.blog.enums.StatusCodeEnum.SUCCESS;
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    public static <T> Result<T> success() {
        return buildResult(null, SUCCESS.getCode(), SUCCESS.getMsg());
    }

    public static <T> Result<T> success(T data) {
        return buildResult(data, SUCCESS.getCode(), SUCCESS.getMsg());
    }

    public static <T> Result<T> fail(String message) {
        return buildResult(null,  FAIL.getCode(), message);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return buildResult( null, code, message);
    }

    private static <T> Result<T> buildResult(T data, Integer code, String message) {
        Result<T> r = new Result<>();
        r.setData(data);
        r.setCode(code);
        r.setMsg(message);
        return r;
    }

}