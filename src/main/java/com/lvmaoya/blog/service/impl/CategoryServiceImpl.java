package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.mapper.CategoryMapper;
import com.lvmaoya.blog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private BlogMapper blogMapper;
    @Override
    public Result<List<Category>> getCategoryList() {
        List<Blog> blogList = blogMapper.selectList(null);
        Set<String> longStream = blogList.stream().map(Blog::getCategoryId).collect(Collectors.toSet());

        List<Category> categories = listByIds(longStream);

        List<Category> collect = categories.stream().filter(category -> "2".equals(category.getFatherCategoryName())).collect(Collectors.toList());

        return Result.success(collect);
    }
}
