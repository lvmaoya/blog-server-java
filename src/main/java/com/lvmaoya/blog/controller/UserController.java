package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("list")
    public List<UserVo> getUsers() {
        return userService.userList(null);
    }

}
