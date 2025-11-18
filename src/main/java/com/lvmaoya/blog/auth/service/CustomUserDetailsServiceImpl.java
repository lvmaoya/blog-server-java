package com.lvmaoya.blog.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvmaoya.blog.role.entity.Role;
import com.lvmaoya.blog.user.entity.User;
import com.lvmaoya.blog.auth.entity.CustomUserDetails;
import com.lvmaoya.blog.role.mapper.RoleMapper;
import com.lvmaoya.blog.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 根据用户名查询用户信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        // 2. 判断是否是查到的用户，没有查到抛出异常
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // 3. 查询用户角色及权限
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        user.setRoles(roles);

        // 4. 返回用户信息
        // TODO 查询用户权限
        return new CustomUserDetails(user);
    }
}
