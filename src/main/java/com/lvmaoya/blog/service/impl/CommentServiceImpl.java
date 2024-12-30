package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.mapper.CommentMapper;
import com.lvmaoya.blog.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Override
    public boolean postComment() {
        return false;
    }
}
