package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.vo.BlogTimeRangeStatsVO;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.BlogStatisticsService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/blog/stats")
@RequiredArgsConstructor
public class BlogStatisticsController {

    @Resource
    private final BlogStatisticsService blogStatisticsService;

    @GetMapping
    public R getBlogStatsByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {

        BlogTimeRangeStatsVO stats = blogStatisticsService.getBlogStatsByTimeRange(startTime, endTime);
        return R.success(stats);
    }

    @GetMapping("/statistics")
    public R<Map<String, Object>> getBlogStatistics() {
        return R.success(blogStatisticsService.getBlogStatistics());
    }
}