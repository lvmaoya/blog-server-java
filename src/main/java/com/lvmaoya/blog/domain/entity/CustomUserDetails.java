package com.lvmaoya.blog.domain.entity;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
public class CustomUserDetails implements UserDetails, Serializable {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

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