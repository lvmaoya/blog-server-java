package com.lvmaoya.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("content")
public class BlogContent {
    private Long id;
    private String content;
}
