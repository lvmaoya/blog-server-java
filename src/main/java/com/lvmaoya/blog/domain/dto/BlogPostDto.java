package com.lvmaoya.blog.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BlogPostDto {
    private Integer id;
    private String title;
    private String description;
    private String keywords;
    private Integer categoryId;
    private Integer fatherCategoryId;
    private String coverImage;
    private int status;
    private Integer authorId;
    private int top;
    private String content;
}
