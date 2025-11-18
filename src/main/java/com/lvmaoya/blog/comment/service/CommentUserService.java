package com.lvmaoya.blog.comment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.comment.entity.CommentUser;
import com.lvmaoya.blog.comment.mapper.CommentUserMapper;
import org.springframework.stereotype.Service;

@Service
public class CommentUserService extends ServiceImpl<CommentUserMapper, CommentUser> {

}
