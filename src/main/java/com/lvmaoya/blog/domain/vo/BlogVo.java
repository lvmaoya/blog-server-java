package com.lvmaoya.blog.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lvmaoya.blog.domain.entity.Category;
import lombok.Data;

import java.util.Date;

@Data
public class BlogVo {
    private String id;
    private String title;
    private String description;
    private Category category;
    private String categoryId;
    private String fatherCategoryId;
    private String coverImage;
    private String content;
    private int pageView;
    private int preferNum;
    private int status;
    private int privacy;
    private String authorId;
    private int top;
    private int deleted;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date draftTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date deletedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updatedTime;
}
