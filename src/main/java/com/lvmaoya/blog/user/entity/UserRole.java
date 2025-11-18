package com.lvmaoya.blog.user.entity;

import lombok.Data;

/**
* 用户角色关联表
 */
@Data
public class UserRole {

    /**
    * 用户ID
    */
    private Integer userId;
    /**
    * 角色ID
    */
    private Integer roleId;
}
