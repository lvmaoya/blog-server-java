package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;

public interface CommentService extends IService<Comment> {

    boolean addOrUpdateComment(Comment comment);

    IPage<Comment> selectList(CommentSearchParams commentSearchParams);
}
