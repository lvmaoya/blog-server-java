package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.entity.User;
import com.lvmaoya.blog.domain.vo.LoginUserVo;
import com.lvmaoya.blog.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthController {

    @Resource
    AuthService authService;

    @PostMapping("/login")
    public LoginUserVo login(@RequestBody User user) {
        if (!StringUtils.hasText(user.getName())){
            throw new IllegalArgumentException();
        }
        return authService.login(user.getName(), user.getPassword());
    }

    @GetMapping("/logout")
    public Object logout() {
        return authService.logout();
    }
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
       authService.getCaptcha(request, response);
    }

}
