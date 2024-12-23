package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.User;
import com.lvmaoya.blog.domain.entity.LoginUser;
import com.lvmaoya.blog.domain.vo.LoginUserVo;
import com.lvmaoya.blog.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public Result<LoginUserVo> login(@RequestBody User user) {
        return authService.login(user.getName(), user.getPassword());
    }
}
