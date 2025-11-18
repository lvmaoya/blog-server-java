package com.lvmaoya.blog.blog.pojo;

import lombok.Data;

@Data
public class BlogForm {
    private Integer id;
    private String title;
    private String description;
    private String keywords;
    private Integer categoryId;
    private Long charCount;
    private Integer pageView;
    private Integer fatherCategoryId;
    private String coverImage;
    private Integer status;
    private Integer authorId;
    private Integer top;
    private String content;
    private Boolean keepDesc;
}
