package com.lvmaoya.blog.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
* 角色表
 */
@Data
public class Permission{

    /**
    * 角色ID主键
    */
    private Integer permissionId;
    /**
    * 角色名
    */
    private String permission_name;
}
