package com.lvmaoya.blog.domain.entity;
import lombok.Data;

import java.io.Serializable;

/**
* 角色权限关联表
*/
@Data
public class RolePermission implements Serializable {

    /**
    * 角色ID
    */
    private Long roleId;
    /**
    * 权限ID
    */
    private Long permissionId;
}
