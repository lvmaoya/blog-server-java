package com.lvmaoya.blog.service;

import com.lvmaoya.blog.domain.vo.LoginUserVo;
import com.lvmaoya.blog.domain.vo.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthService {
    R login(String username, String password);
    R logout();

    void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
