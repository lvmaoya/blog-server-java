package com.lvmaoya.blog.comment.pojo;

import lombok.Data;

@Data
public class CommentStatusUpdateForm {
    private Integer commentId;
    private Integer status;
}