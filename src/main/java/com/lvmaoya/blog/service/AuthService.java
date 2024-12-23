package com.lvmaoya.blog.service;

import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.LoginUser;
import com.lvmaoya.blog.domain.vo.LoginUserVo;

public interface AuthService {
     Result<LoginUserVo> login(String username, String password);
}
