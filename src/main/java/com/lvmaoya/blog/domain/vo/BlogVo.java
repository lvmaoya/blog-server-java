package com.lvmaoya.blog.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lvmaoya.blog.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogVo {
    private Integer id;
    private String title;
    private String description;
    private String keywords;
    private String articleAbstract;
    private Category category;
    private Integer categoryId;
    private Integer fatherCategoryId;
    private String coverImage;
    private String content;
    private Long charCount;
    private Integer pageView;
    private Integer preferNum;
    private Integer status;
    private Integer authorId;
    private Integer top;
//    private int deleted;
    @TableField(fill = FieldFill.INSERT) // 第一次插入时自动填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date publishedTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) //第一次插入时自动填充 更新时自动填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updatedTime;
}
