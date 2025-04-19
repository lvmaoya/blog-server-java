package com.lvmaoya.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class User  implements Serializable {
    private Integer id;
    private String username;
    private String password;
    private String nickName;
    private String email;
    private String mobile;
    private String otherContact;
    private String introduce;
    private String avatar;
    private String createdTime;
    private String updatedTime;

    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private List<Role> roles;
}
