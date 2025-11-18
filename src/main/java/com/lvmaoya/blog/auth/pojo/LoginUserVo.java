package com.lvmaoya.blog.auth.pojo;

import com.lvmaoya.blog.user.pojo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserVo implements Serializable {
    private String token;
    private UserVo user;
}
