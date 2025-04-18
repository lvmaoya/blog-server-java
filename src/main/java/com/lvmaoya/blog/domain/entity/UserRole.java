package com.lvmaoya.blog.domain.entity;

import lombok.Data;

import java.io.Serializable;

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
