package com.lvmaoya.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("content")
@AllArgsConstructor
@NoArgsConstructor
public class BlogContent {
    private Integer id;
    private String content;
}
