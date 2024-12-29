package com.lvmaoya.blog.service;

import com.lvmaoya.blog.domain.vo.LoginUserVo;

public interface AuthService {
    LoginUserVo login(String username, String password);
    Object logout();
}
