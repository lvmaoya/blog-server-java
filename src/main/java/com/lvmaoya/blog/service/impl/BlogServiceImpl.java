package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.BlogContent;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.service.CategoryService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private BlogContentMapper blogContentMapper;
    @Resource
    private CategoryService categoryService;

    @Override
    public Result<IPage<BlogVo>> blogList(Integer page, Integer size,Integer status, Integer top) {

        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Blog::getPublishedTime);
        queryWrapper.orderByAsc(Blog::getTop);
        queryWrapper.eq(Objects.nonNull(status), Blog::getStatus, status);

        Page<Blog> pageObj = new Page<>(page, size);
        List<Blog> blogList = blogMapper.selectList(pageObj, queryWrapper);



        List<BlogVo> blogVos = BeanCopyUtil.copyBeanList(blogList, BlogVo.class);
        for (BlogVo blog : blogVos) {
            Category category = categoryService.getById(blog.getCategoryId());
            blog.setCategory(category);
        }

        //使用stream实现

        Page<BlogVo> pageVo = new Page<>();
        pageVo.setSize(pageObj.getSize());
        pageVo.setTotal(pageObj.getTotal());
        pageVo.setRecords(blogVos);
        pageVo.setPages(pageObj.getPages());
        pageVo.setCurrent(pageObj.getCurrent());

        return Result.success(pageVo);
    };

    public Result<BlogVo> getBlogById(String id) {
        Blog blog = blogMapper.selectById(id);
        BlogContent blogContent = blogContentMapper.selectById(id);
        BlogVo blogVo = BeanCopyUtil.copyBean(blog, BlogVo.class);
        blogVo.setContent(blogContent.getContent());
        return Result.success(blogVo);
    }
}
