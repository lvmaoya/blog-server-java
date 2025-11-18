package com.lvmaoya.blog.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.user.entity.User;
import com.lvmaoya.blog.user.pojo.UserVo;
import com.lvmaoya.blog.user.mapper.UserMapper;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Resource
    private UserMapper userMapper;

    public List<UserVo> userList(QueryWrapper<User> wrapper) {
        List<User> users = userMapper.selectList(wrapper);
        return BeanCopyUtil.copyBeanList(users,UserVo.class);
    }

    public UserVo getUser(int i) {
        User user = userMapper.selectById(i);
        UserVo userVo = BeanCopyUtil.copyBean(user, UserVo.class);
        return userVo;
    }
}
