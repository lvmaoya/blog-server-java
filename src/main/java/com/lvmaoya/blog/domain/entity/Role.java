package com.lvmaoya.blog.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
* 角色表
 */
@Data
public class Role implements Serializable {

    /**
    * 角色ID主键
    */
    private Long roleId;
    /**
    * 角色名
    */
    private String roleName;
}
