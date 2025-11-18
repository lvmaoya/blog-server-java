package com.lvmaoya.blog.comment.pojo;

import lombok.Data;

@Data
public class CommentForm {
    private Integer id;
    private Integer articleId;
    private Integer rootCommentId;
    private Integer type;
    private String toCommentId;
    private String avatar;
    private String username;
    private String email;
    private String site;
    private String content;
    private int status;
}
