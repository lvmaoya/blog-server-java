package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Comment;

public interface CommentService extends IService<Comment> {
    boolean postComment();
}
