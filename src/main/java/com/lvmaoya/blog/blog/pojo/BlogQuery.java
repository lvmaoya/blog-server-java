package com.lvmaoya.blog.blog.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class BlogQuery {
    private Integer page;
    private Integer size;
    private String title;
    private String keywords;
    private Integer categoryId;
    private Integer fatherCategoryId;
    private List<Integer> fatherCategoryIds;
    private String sortBy;
    private String sortOrder;
    private String status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedEnd;
}
