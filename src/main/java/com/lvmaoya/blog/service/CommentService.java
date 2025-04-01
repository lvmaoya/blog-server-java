package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.R;

public interface CommentService extends IService<Comment> {

    R addOrUpdateComment(Comment comment);

    R selectList(CommentSearchParams commentSearchParams);
}
