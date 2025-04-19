package com.lvmaoya.blog.handler.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvmaoya.blog.domain.vo.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 默认错误消息
        String errorMessage = "认证失败，请提供有效的认证信息";
        int code = HttpServletResponse.SC_UNAUTHORIZED;

        // 针对不同类型的认证异常提供更具体的错误信息
        if (authException instanceof BadCredentialsException) {
            errorMessage = "用户名或密码错误";
            code = 4001; // 可以定义业务错误码
        } else if (authException instanceof InsufficientAuthenticationException) {
            errorMessage = "请求未包含认证信息";
            code = 4002;
        } else if (authException instanceof AccountExpiredException) {
            errorMessage = "账号已过期";
            code = 4003;
        } else if (authException instanceof LockedException) {
            errorMessage = "账号已被锁定";
            code = 4004;
        } else if (authException instanceof DisabledException) {
            errorMessage = "账号已被禁用";
            code = 4005;
        } else if (authException instanceof CredentialsExpiredException) {
            errorMessage = "凭证已过期";
            code = 4006;
        }

        // 使用R类构建统一响应
        R<Void> result = R.error(code, errorMessage);

        objectMapper.writeValue(response.getOutputStream(), result);
    }
}