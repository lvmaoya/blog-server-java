package com.lvmaoya.blog.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
* 用户角色关联表
 */
@Data
public class UserRole implements Serializable {

    /**
    * 用户ID
    */
    private Long userId;
    /**
    * 角色ID
    */
    private Long roleId;
}
