package com.lvmaoya.blog.handler.exception;

import com.lvmaoya.blog.domain.vo.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（如分类非空删除）
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e, HttpServletResponse response) {
        log.warn("Business Exception: code={}, message={}", e.getCode(), e.getMessage());
        response.setStatus(e.getCode());
        return R.error(400, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletResponse response) {
        log.error("RuntimeException occurred: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return e.getMessage();
    }


    @ExceptionHandler(NullPointerException.class)
    public Object handleNullPointerException(NullPointerException e, HttpServletResponse response) {
        log.error("NullPointerException occurred: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return e.getMessage();
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        log.error("IllegalArgumentException occurred: ", e);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return R.error(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception e, HttpServletResponse response) {
        log.error("General Exception occurred: ", e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return e.getMessage();
    }
}
