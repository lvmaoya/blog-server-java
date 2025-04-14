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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

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

    @Override
    public Map<String, Object> getBlogStatistics() {
        // 计算上个月的时间范围
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Date startDate = Date.from(firstDayOfLastMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(lastDayOfLastMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 获取基本统计信息
        Map<String, Object> statistics = blogMapper.getBlogStatistics(startDate, endDate);

        // 获取按 fatherCategoryId 统计的信息
        List<Map<String, Object>> categoryCountList = blogMapper.getBlogCountByFatherCategoryId();

        // 构建包含 father_category_id 1 - 4 的结果列表
        List<Map<String, Object>> finalCategoryCountList = new ArrayList<>();
        Set<Integer> targetIds = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        Map<Integer, Long> countMap = new HashMap<>(); // 修改为 Long 类型

        // 先将数据库查询结果存入 countMap
        for (Map<String, Object> item : categoryCountList) {
            Integer fatherCategoryId = (Integer) item.get("father_category_id");
            Long count = (Long) item.get("count"); // 修改为 Long 类型
            countMap.put(fatherCategoryId, count);
        }

        // 确保结果列表包含 father_category_id 1 - 4
        for (int id : targetIds) {
            Map<String, Object> resultItem = new HashMap<>();
            resultItem.put("father_category_id", id);
            resultItem.put("count", countMap.getOrDefault(id, 0L).intValue()); // 使用 intValue() 转换为 Integer
            finalCategoryCountList.add(resultItem);
        }

        // 对结果列表按 father_category_id 排序
        finalCategoryCountList.sort(Comparator.comparingInt(item -> (Integer) item.get("father_category_id")));

        statistics.put("categoryCountList", finalCategoryCountList);

        return statistics;
    }
}