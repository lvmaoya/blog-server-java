package com.lvmaoya.blog.auth.controller;

import com.lvmaoya.blog.auth.pojo.LoginForm;
import com.lvmaoya.blog.auth.pojo.LoginUserVo;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    final AuthService authService;

    @PostMapping("/login")
    public R<LoginUserVo> login(@Valid @RequestBody LoginForm user, HttpServletRequest request) {
        return authService.login(user.getUsername(), user.getPassword(), user.getCaptcha(), request);
    }

    @GetMapping("/logout")
    public R<?> logout() {
        return authService.logout();
    }

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
       authService.getCaptcha(request, response);
    }

}
