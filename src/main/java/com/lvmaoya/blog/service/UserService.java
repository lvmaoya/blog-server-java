package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.User;
import com.lvmaoya.blog.domain.vo.UserVo;

import java.util.List;

public interface UserService extends IService<User> {
    List<UserVo> userList(QueryWrapper<User> wrapper);

    UserVo getUser(int i);
}
