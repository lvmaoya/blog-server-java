package com.lvmaoya.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.vo.BlogTimeRangeStatsVO;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    /**
     * 按时间范围统计博客数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据列表
     */
    @Select("<script>" +
            "SELECT " +
            "DATE_FORMAT(published_time, '%Y-%m') as timeRange, " +
            "COUNT(*) as articleCount, " +
            "SUM(page_view) as pageView, " +
            "SUM(prefer_num) as preferNum, " +
            "SUM(char_count) as charCount " +
            "FROM blog " +
            "WHERE published_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 " +
            "GROUP BY DATE_FORMAT(published_time, '%Y-%m') " +
            "ORDER BY timeRange" +
            "</script>")
    List<TimeRangeStatsDTO> countBlogStatsByTimeRange(@Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime);


    /**
     * 获取文章统计信息
     * @param lastMonthStart 上个月开始时间
     * @param lastMonthEnd 上个月结束时间
     * @return 包含统计信息的 Map
     */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM blog) AS totalBlogCount, " +
            "(SELECT COUNT(*) FROM blog WHERE published_time BETWEEN #{lastMonthStart} AND #{lastMonthEnd}) AS lastMonthBlogCount, " +
            "(SELECT SUM(char_count) FROM blog) AS totalCharCount, " +
            "(SELECT SUM(char_count) FROM blog WHERE published_time BETWEEN #{lastMonthStart} AND #{lastMonthEnd}) AS lastMonthCharCount, " +
            "(SELECT SUM(page_view) FROM blog) AS totalPageView, " +
            "(SELECT SUM(page_view) FROM blog WHERE published_time BETWEEN #{lastMonthStart} AND #{lastMonthEnd}) AS lastMonthPageView")
    Map<String, Object> getBlogStatistics(Date lastMonthStart, Date lastMonthEnd);


    /**
     * 按 fatherCategoryId 统计文章数量并返回列表
     * @return 包含父分类 ID 和对应文章数量的列表
     */
    @Select("SELECT father_category_id, COUNT(*) AS count FROM blog GROUP BY father_category_id")
    List<Map<String, Object>> getBlogCountByFatherCategoryId();

    @Data
    class TimeRangeStatsDTO {
        private String timeRange;
        private Integer articleCount;
        private Integer pageView;
        private Integer charCount;
        private Integer preferNum;
    }
}
