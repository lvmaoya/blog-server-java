package com.lvmaoya.blog.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvmaoya.blog.blog.entity.Blog;
import com.lvmaoya.blog.blog.pojo.BlogVo;
import lombok.Data;
import org.apache.ibatis.annotations.*;

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

    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "category.id", column = "category_id"),
            @Result(property = "category.categoryName", column = "category_name"),
            @Result(property = "category.fatherCategoryId", column = "father_category_id"),
            // 同时映射单独的categoryId和fatherCategoryId字段
            @Result(property = "categoryId", column = "category_id"),
            @Result(property = "fatherCategoryId", column = "father_category_id")
    })
    @Select("<script>\n" +
            "SELECT b.*, c.category_name \n" +
            "FROM blog b \n" +
            "LEFT JOIN category c ON b.category_id = c.id \n" +
            "<where>\n"  +
            "   b.deleted = FALSE\n" +  // 新增删除状态过滤
            "   <if test=\"status != null and status != ''\">\n" +
            "       AND b.status = #{status}\n" +
            "   </if>\n" +
            "   <if test=\"categoryId != null\">\n" +
            "       AND b.category_id = #{categoryId}\n" +
            "   </if>\n" +
            "   <if test=\"fatherCategoryId != null\">\n" +
            "       AND c.father_category_id = #{fatherCategoryId}\n" +
            "   </if>\n" +
            "   <if test=\"fatherCategoryIds != null and fatherCategoryIds.size() > 0\">\n" +
            "       AND c.father_category_id IN \n" +
            "       <foreach collection=\"fatherCategoryIds\" item=\"id\" open=\"(\" separator=\",\" close=\")\">\n" +
            "           #{id}\n" +
            "       </foreach>\n" +
            "   </if>\n" +
            "   <if test=\"title != null and title != ''\">\n" +
            "       AND b.title LIKE CONCAT('%', #{title}, '%')\n" +
            "   </if>\n" +
            "   <if test=\"keywords != null and keywords != ''\">\n" +
            "       AND b.description LIKE CONCAT('%', #{keywords}, '%')\n" +
            "   </if>\n" +
            "   <if test=\"publishedStart != null\">\n" +
            "       AND b.published_time &gt;= #{publishedStart}\n" +  // 注意这里的&gt;转义
            "   </if>\n" +
            "   <if test=\"publishedEnd != null\">\n" +
            "       AND b.published_time &lt;= #{publishedEnd}\n" +   // 注意这里的&lt;转义
            "   </if>\n" +
            "</where>\n" +
            "<if test=\"sortBy != null and sortBy != ''\">\n" +
            "   ORDER BY b.${sortBy} \n" +
            "   <choose>\n" +
            "       <when test=\"sortOrder != null and sortOrder != ''\">\n" +
            "           ${sortOrder}\n" +
            "       </when>\n" +
            "       <otherwise>DESC</otherwise>\n" +
            "   </choose>\n" +
            "</if>\n" +
            "<if test=\"sortBy == null or sortBy == ''\">\n" +
            "   ORDER BY b.published_time DESC\n" +
            "</if>\n" +
            "</script>")
    Page<BlogVo> selectBlogWithCategoryPage(Page<Blog> page,
                                            @Param("status") String status,
                                            @Param("categoryId") Integer categoryId,
                                            @Param("fatherCategoryId") Integer fatherCategoryId,
                                            @Param("fatherCategoryIds") List<Integer> fatherCategoryIds, // 新增参数
                                            @Param("title") String title,
                                            @Param("keywords") String keywords,
                                            @Param("publishedStart") Date publishedStart,
                                            @Param("publishedEnd") Date publishedEnd,
                                            @Param("sortBy") String sortBy,
                                            @Param("sortOrder") String sortOrder);

    @Select("SELECT b.*, c.content FROM blog b " +
            "LEFT JOIN content c ON b.id = c.id " +
            "WHERE b.id = #{id} AND b.deleted = 0")
    @Results({
            @Result(property = "category", column = "category_id",
                    one = @One(select = "com.lvmaoya.blog.category.mapper.CategoryMapper.selectById"))
    })
    BlogVo selectBlogWithContentById(Integer id);

    @Data
    class TimeRangeStatsDTO {
        private String timeRange;
        private Integer articleCount;
        private Integer pageView;
        private Integer charCount;
        private Integer preferNum;
    }
}
