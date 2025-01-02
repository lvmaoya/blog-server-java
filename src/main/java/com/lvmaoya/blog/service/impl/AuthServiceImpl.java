package com.lvmaoya.blog.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.lvmaoya.blog.domain.entity.LoginUser;
import com.lvmaoya.blog.domain.vo.LoginUserVo;
import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.mapper.UserMapper;
import com.lvmaoya.blog.service.AuthService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import com.lvmaoya.blog.utils.JwtUtil;
import com.lvmaoya.blog.utils.RedisCacheUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    UserMapper userMapper;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    RedisCacheUtil redisCacheUtil;

    @Override
    public LoginUserVo login(String username, String password) {



        // 1. 使用authenticationManager来完成用户的认证，默认是没有这个Bean的，所以需要在配置类中来注入
        // 2. 调用authenticationManager.authenticate完成认证，默认使用UserDetail生成的账号而不是使用来自于自己数据库中的账号密码数据，所有需要重写其认证实现接口

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误！");
        }

        //获取userId生成token
        LoginUser loginUser = (LoginUser)authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.generateToken(userId);

        UserVo userVo = BeanCopyUtil.copyBean(loginUser.getUser(), UserVo.class);
        LoginUserVo loginUserVo = new LoginUserVo(jwt,userVo);

        //把用户信息存入redis

        redisCacheUtil.set("blogLogin"+userId, loginUserVo);
        System.out.println(redisCacheUtil.get("blogLogin"+userId));;

        //把token和user封装、返回

        return loginUserVo;
    }

    @Override
    public Object logout() {
        // 解析token获取到用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUserVo user = (LoginUserVo)authentication.getPrincipal();
        Long id = user.getUser().getId();

        // 删除redis登录
        redisCacheUtil.delete("blogLogin"+id);
        return "success";
    }

    @Override
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 定义图形验证码的宽、高、验证码字符数、干扰线的条数
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 30, 4, 20);


        // 获取验证码中的文本
        String code = lineCaptcha.getCode();


        // 将验证码存储在会话中，用于后续验证
        request.getSession().setAttribute("captcha", code);


        // 设置响应的内容类型为图片
        response.setContentType("image/png");


        // 获取输出流
        ServletOutputStream outputStream = response.getOutputStream();


        // 将验证码写出到输出流
        lineCaptcha.write(outputStream);


        // 关闭输出流
        outputStream.close();
    }
}
