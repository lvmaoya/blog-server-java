package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.CommentUser;
import com.lvmaoya.blog.domain.entity.User;
import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.mapper.CommentUserMapper;
import com.lvmaoya.blog.mapper.UserMapper;
import com.lvmaoya.blog.service.CommentUserService;
import com.lvmaoya.blog.service.UserService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentUserServiceImpl extends ServiceImpl<CommentUserMapper, CommentUser> implements CommentUserService {

}
