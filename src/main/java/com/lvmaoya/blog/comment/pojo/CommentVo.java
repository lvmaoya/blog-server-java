package com.lvmaoya.blog.comment.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("commentary")
public class CommentVo {
    private Integer id;
    private Integer articleId;
    private String articleTitle;
    private Integer rootCommentId;
    private Integer type;
    private String toCommentId;
    private String content;
    private Integer status;
    private Integer deleted;

    // User information fields
    private String username;
    private String avatar;
    private String email;
    private String site;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updatedTime;
}