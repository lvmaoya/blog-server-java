package com.lvmaoya.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
