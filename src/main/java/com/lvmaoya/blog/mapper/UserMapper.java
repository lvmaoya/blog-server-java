package com.lvmaoya.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.domain.entity.Role;
import com.lvmaoya.blog.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT r.* FROM role r, user_role ur WHERE r.role_id = ur.role_id AND ur.user_id = #{userId}")
    Role selectRoleByUserId(Integer userId);
}
