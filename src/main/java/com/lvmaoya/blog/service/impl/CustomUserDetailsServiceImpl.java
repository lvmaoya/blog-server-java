package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lvmaoya.blog.domain.entity.Role;
import com.lvmaoya.blog.domain.entity.User;
import com.lvmaoya.blog.domain.entity.CustomUserDetails;
import com.lvmaoya.blog.mapper.RoleMapper;
import com.lvmaoya.blog.mapper.UserMapper;
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

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        // 判断是否是查到的用户，没有查到抛出异常
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // 2. 查询用户角色及权限
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        user.setRoles(roles);

        // 返回用户信息
        // TODO 查询用户权限
        return new CustomUserDetails(user);
    }
}
