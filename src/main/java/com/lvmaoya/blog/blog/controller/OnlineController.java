package com.lvmaoya.blog.blog.controller;

import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.blog.service.OnlineService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnlineController {

    @Resource
    private OnlineService onlineService;

    @GetMapping("/online/count")
    public R getOnlineCount() {
        return R.success(onlineService.getOnlineCount());
    }
}