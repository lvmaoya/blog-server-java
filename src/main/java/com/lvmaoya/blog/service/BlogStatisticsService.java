package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.vo.BlogTimeRangeStatsVO;

import java.util.Date;

public interface BlogStatisticsService{

    BlogTimeRangeStatsVO getBlogStatsByTimeRange(Date startTime, Date endTime);
}
