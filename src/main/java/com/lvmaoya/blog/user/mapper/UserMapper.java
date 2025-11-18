package com.lvmaoya.blog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.role.entity.Role;
import com.lvmaoya.blog.user.entity.User;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT r.* FROM role r, user_role ur WHERE r.role_id = ur.role_id AND ur.user_id = #{userId}")
    Role selectRoleByUserId(Integer userId);

    @Select("SELECT r.* FROM role r " +
            "JOIN user_role ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    @Result(property = "permissions", column = "roleId",
            many = @Many(select = "com.lvmaoya.blog.role.mapper.RoleMapper.selectPermissionsByRoleId"))
    List<Role> selectRolesByUserId(Integer userId);
}
