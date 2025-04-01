package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.handler.exception.BusinessException;
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
    public R removeById(String id) {
        // 判断是否有这个类别
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            R.success();
        }else{
            if(Objects.equals(category.getFatherCategoryId(),null)){
                throw new BusinessException(400, "此类不可删除");
            }
        }
        // 查询该类别下是否有文章
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getCategoryId,id);
        Long l = blogMapper.selectCount(queryWrapper);

        if(l > 0){
            throw new BusinessException(400, "该类别下仍有" + l + "篇文章");
        }

        int i = categoryMapper.deleteById(id);
        return R.success(i > 0);
    }
}
