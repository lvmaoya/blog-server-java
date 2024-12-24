package com.lvmaoya.blog.handler.exception;

import com.lvmaoya.blog.domain.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e, HttpServletResponse response) {
        log.error("RuntimeException occurred: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Runtime error occurred: " + e.getMessage());
    }


    @ExceptionHandler(NullPointerException.class)
    public Result<Object> handleNullPointerException(NullPointerException e, HttpServletResponse response) {
        log.error("NullPointerException occurred: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Null pointer error occurred: " + e.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        log.error("IllegalArgumentException occurred: ", e);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return Result.fail(HttpServletResponse.SC_BAD_REQUEST,"Invalid argument: " + e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Result<Object> handleGeneralException(Exception e, HttpServletResponse response) {
        log.error("General Exception occurred: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return Result.fail(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"An unexpected error occurred: " + e.getMessage());
    }
}
