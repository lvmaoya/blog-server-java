package com.lvmaoya.blog.domain.entity;
import lombok.Data;

import java.io.Serializable;

/**
* 角色权限关联表
*/
@Data
public class RolePermission{

    /**
    * 角色ID
    */
    private Integer roleId;
    /**
    * 权限ID
    */
    private Integer permissionId;
}
