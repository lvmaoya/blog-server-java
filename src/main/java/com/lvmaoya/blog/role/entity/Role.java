package com.lvmaoya.blog.role.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lvmaoya.blog.permission.entity.Permission;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* 角色表
 */
@Data
public class Role  implements Serializable {

    /**
    * 角色ID主键
    */
    private Integer roleId;
    /**
    * 角色名
    */
    private String roleName;


    // 非数据库字段
    @TableField(exist = false)
    private List<Permission> permissions;
}
