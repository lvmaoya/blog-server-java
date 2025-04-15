package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.OnlineService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnlineController {
    @Autowired
    private OnlineService onlineService;

    @PostMapping("/heartbeat")
    public R heartbeat(@RequestBody HeartbeatRequest request) {
        onlineService.heartbeat(request.getClientId());
        return R.success();
    }

    @GetMapping("/online/count")
    public R getOnlineCount() {
        return R.success(onlineService.getOnlineCount());
    }

    @Data
    public static class HeartbeatRequest {
        private String clientId;
        // getter/setter
    }
}