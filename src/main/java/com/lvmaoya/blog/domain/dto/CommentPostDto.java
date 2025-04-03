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
    private Integer rootId; // 评论的根评论
    private Integer toUserId;
    private String toUserName;
    private Integer avatar;
    private String userName;
    private String email;
    private String site;
    private String content;
    private int status;
}
