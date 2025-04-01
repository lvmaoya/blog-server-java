package com.lvmaoya.blog.domain.dto;

import lombok.Data;

@Data
public class CategoryPostDto {
    private Integer id;
    private String categoryName;
    private Integer fatherCategoryId;
}
