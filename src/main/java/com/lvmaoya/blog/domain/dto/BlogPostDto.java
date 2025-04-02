package com.lvmaoya.blog.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BlogPostDto {
    private Integer id;
    private String title;
    private String description;
    private String keywords;
    private Integer categoryId;
    private Integer fatherCategoryId;
    private String coverImage;
    private int status;
    private Integer authorId;
    private int top;
    private String content;

    @Data
    @TableName("commentary")
    public static class CommentDto {
        private Integer id;
        private Integer articleId;
        private int type; // 0：文章评论，1：评论评论
        private Integer rootId; // 评论的根评论
        private Integer toUserId;
        private String toUserName;
        private Integer avatar;
        private String userName;
        private String email;
        private String content;
        private int status;
        private int preferNumber;
        private int deleted;
        @TableField(fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
        private Date createdTime;
        @TableField(fill = FieldFill.INSERT_UPDATE)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
        private Date updatedTime;
    }
}
