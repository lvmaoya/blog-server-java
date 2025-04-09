package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.vo.BlogTimeRangeStatsVO;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.service.BlogStatisticsService;
import com.lvmaoya.blog.utils.RedisCacheUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogStatisticsServiceImpl implements BlogStatisticsService {

    @Resource
    private final BlogMapper blogMapper;
    @Resource
    private final RedisCacheUtil redisCacheUtil;

    @Override
    @Transactional
    public BlogTimeRangeStatsVO getBlogStatsByTimeRange(Date startTime, Date endTime) {
        // 1. 验证时间范围
        validateTimeRange(startTime, endTime);

        // 2. 生成缓存key
        String cacheKey = generateCacheKey(startTime, endTime);

        redisCacheUtil.delete(cacheKey);

        // 3. 检查缓存
        BlogTimeRangeStatsVO cachedStats = (BlogTimeRangeStatsVO) redisCacheUtil.get(cacheKey);
        if (cachedStats != null) {
            return cachedStats;
        }

        // 4. 查询数据库
        List<BlogMapper.TimeRangeStatsDTO> statsList = blogMapper.countBlogStatsByTimeRange(startTime, endTime);

        // 5. 处理结果
        BlogTimeRangeStatsVO result = new BlogTimeRangeStatsVO();
        result.setTimeRange(new ArrayList<>());
        result.setArticleCount(new ArrayList<>());
        result.setPageView(new ArrayList<>());
        result.setPreferNum(new ArrayList<>());
        result.setCharCount(new ArrayList<>());

        statsList.forEach(stat -> {
            result.getTimeRange().add(stat.getTimeRange());
            result.getArticleCount().add(stat.getArticleCount() != null ? stat.getArticleCount() : 0);
            result.getPageView().add(stat.getPageView() != null ? stat.getPageView() : 0);
            result.getPreferNum().add(stat.getPreferNum() != null ? stat.getPreferNum() : 0);
            result.getCharCount().add(stat.getCharCount() != null ? stat.getCharCount() : 0);
        });

        // 6. 设置缓存(1小时)
        redisCacheUtil.set(cacheKey, result, 3600);

        return result;
    }

    private void validateTimeRange(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        if (startTime.after(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
    }

    private String generateCacheKey(Date startTime, Date endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return "blog:stats:range:" + sdf.format(startTime) + "-" + sdf.format(endTime);
    }
}