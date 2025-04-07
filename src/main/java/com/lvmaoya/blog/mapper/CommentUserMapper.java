package com.lvmaoya.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.entity.CommentUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentUserMapper extends BaseMapper<CommentUser> {
}
