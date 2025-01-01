package com.lvmaoya.blog.domain.searchParams;

import lombok.Data;

@Data
public class CommentSearchParams {
    private Integer page;
    private Integer size;
    private String articleId;
    private Integer status;
}
