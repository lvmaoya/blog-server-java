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

    @Data
    class TimeRangeStatsDTO {
        private String timeRange;
        private Integer articleCount;
        private Integer pageView;
        private Integer charCount;
        private Integer preferNum;
    }
}
