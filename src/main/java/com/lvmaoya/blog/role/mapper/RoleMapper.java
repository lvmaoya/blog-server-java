package com.lvmaoya.blog.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.permission.entity.Permission;
import com.lvmaoya.blog.role.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    @Select("SELECT p.* FROM permission p, role_permission rp WHERE p.permission_id = rp.permission_id AND rp.role_id = #{roleId}")
    List<Permission> selectPermissionsByRoleId(Integer roleId);
}
