package com.lvmaoya.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {
    private Long id;
    private String name;
    private String password;
    private String nickName;
    private String email;
    private String mobile;
    private String otherContact;
    private String introduce;
    private String avatar;
    private String createdAt;
    private String updatedAt;
    @TableLogic
    private Integer deleted;
}
