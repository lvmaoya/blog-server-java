package com.lvmaoya.blog.auth.entity;

import com.lvmaoya.blog.permission.entity.Permission;
import com.lvmaoya.blog.role.entity.Role;
import com.lvmaoya.blog.user.entity.User;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
public class CustomUserDetails implements UserDetails, Serializable {
    @Resource
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // 添加角色（前缀ROLE_是Spring Security的要求）
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));

                // 添加权限
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
                    }
                }
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}