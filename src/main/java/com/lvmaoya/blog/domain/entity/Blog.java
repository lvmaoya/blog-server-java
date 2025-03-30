package com.lvmaoya.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blog {
    private String id;
    private String title;
    private String description;
    private String keywords;
    private String categoryId;
    private String fatherCategoryId;
    private String coverImage;
    private int pageView;
    private int preferNum;
    private int status;
    private String authorId;
    private int top;
    private int deleted;
    @TableField(fill = FieldFill.INSERT) // 第一次插入时自动填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishedTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) //第一次插入时自动填充 更新时自动填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updatedTime;
}
