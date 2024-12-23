package com.lvmaoya.blog.service.impl;

import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.LoginUser;
import com.lvmaoya.blog.domain.vo.LoginUserVo;
import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.mapper.UserMapper;
import com.lvmaoya.blog.service.AuthService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import com.lvmaoya.blog.utils.JwtUtil;
import com.lvmaoya.blog.utils.RedisCacheUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    UserMapper userMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    RedisCacheUtil redisCacheUtil;

    @Override
    public Result<LoginUserVo> login(String username, String password) {



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

        return Result.success(loginUserVo);
    }
}
