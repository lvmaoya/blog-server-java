package com.lvmaoya.blog.domain.vo;

import com.lvmaoya.blog.domain.vo.UserVo;
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
