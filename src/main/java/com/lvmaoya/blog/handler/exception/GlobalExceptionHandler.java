package com.lvmaoya.blog.handler.exception;

import com.lvmaoya.blog.domain.vo.R;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.security.SignatureException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理认证失败异常（如用户名密码错误）
     */
    @ExceptionHandler(AuthenticationException.class)
    public R handleAuthenticationException(AuthenticationException e, HttpServletResponse response) {
        log.warn("Authentication failed: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return R.error(HttpServletResponse.SC_UNAUTHORIZED, "认证失败: " + e.getMessage());
    }

    /**
     * 处理登录过期或无效token异常
     */
    @ExceptionHandler({
            ExpiredJwtException.class,
            UnsupportedJwtException.class,
            MalformedJwtException.class,
            SignatureException.class
    })
    public R handleJwtException(Exception e, HttpServletResponse response) {
        log.warn("JWT token error: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return R.error(HttpServletResponse.SC_UNAUTHORIZED, "Token无效或已过期，请重新登录");
    }

    /**
     * 处理访问权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        log.warn("Access denied: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return R.error(HttpServletResponse.SC_FORBIDDEN, "权限不足，无法访问该资源");
    }


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

    /**
     * 处理404类异常（接口或静态资源不存在）
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public R<Void> handleNotFound(Exception e, HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        log.warn("Not Found: {} - {}", uri, e.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return R.error(HttpServletResponse.SC_NOT_FOUND, "接口不存在: " + uri);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public Object handleAuthorizationDeniedException(AuthorizationDeniedException e, HttpServletResponse response) {
        throw e;
    }
}
