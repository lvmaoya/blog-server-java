package com.lvmaoya.blog.service;

import com.lvmaoya.blog.domain.vo.LoginUserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthService {
    LoginUserVo login(String username, String password);
    Object logout();

    void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
