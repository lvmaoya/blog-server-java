package com.lvmaoya.blog.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lvmaoya.blog.category.entity.Category;
import com.lvmaoya.blog.category.pojo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT c.*, COUNT(a.id) as count " +
            "FROM category c " +
            "LEFT JOIN blog a ON c.id = a.category_id " +
            "WHERE (#{fatherCategoryId} IS NULL OR c.father_category_id = #{fatherCategoryId}) " +
            "GROUP BY c.id " +
            "ORDER BY c.created_time DESC")
    List<CategoryVO> selectCategoriesWithArticleCount(@Param("fatherCategoryId") Integer fatherCategoryId);

}
