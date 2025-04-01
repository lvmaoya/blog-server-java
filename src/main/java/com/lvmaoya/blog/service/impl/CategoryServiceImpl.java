package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.mapper.CategoryMapper;
import com.lvmaoya.blog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private BlogMapper blogMapper;

    @Override
    public boolean removeById(String id) {
        // 判断是否有这个类别
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("没有此类");
        }else{
            if(!Objects.equals(category.getFatherCategoryId(),"2")){
                throw new RuntimeException("此类不可删除");
            }
        }
        // 查询该类别下是否有文章
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getCategoryId,id);
        Long l = blogMapper.selectCount(queryWrapper);

        if(l > 0){
            throw new RuntimeException("该类别下仍有" + l + "篇文章");
        }

        int i = categoryMapper.deleteById(id);
        return i > 0;
    }
}
