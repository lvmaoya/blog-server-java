package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Override
    public R saveOrUpdateCategory(Category category) {
        // 验证分类名称
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new BusinessException(400, "分类名称不能为空");
        }

        // 去除前后空格
        category.setCategoryName(category.getCategoryName().trim());

        // 检查分类名称长度
        if (category.getCategoryName().length() > 50) {
            throw new BusinessException(400, "分类名称不能超过50个字符");
        }

        // 如果是新增操作(id为null)且分类名称已存在
        if (category.getId() == null && existsByName(category.getCategoryName())) {
            throw new BusinessException(400, "分类名称已存在");
        }

        // 如果是更新操作(id不为null)且分类名称已存在(排除自身)
        if (category.getId() != null && existsByNameAndIdNot(category.getCategoryName(), category.getId())) {
            throw new BusinessException(400, "分类名称已存在");
        }

        if (category.getFatherCategoryId() == null){
            category.setFatherCategoryId(2);
        }

        // 保存或更新
        boolean result = categoryMapper.insertOrUpdate(category);
        return R.success(result);
    }

    @Override
    public boolean existsByName(String name) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_name", name);
        return categoryMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Integer id) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_name", name)
                .ne("id", id);
        return categoryMapper.selectCount(queryWrapper) > 0;
    }
}
