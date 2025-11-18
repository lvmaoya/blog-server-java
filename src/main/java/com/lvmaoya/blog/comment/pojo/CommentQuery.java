package com.lvmaoya.blog.comment.pojo;

import lombok.Data;

@Data
public class CommentQuery {
    private Integer page;
    private Integer size;
    private Integer articleId;
    private Integer status;
}
