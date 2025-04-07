package com.lvmaoya.blog.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CommentPostDto {
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
