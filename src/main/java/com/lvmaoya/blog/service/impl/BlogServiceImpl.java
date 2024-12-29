package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.BlogContent;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.service.CategoryService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private BlogContentMapper blogContentMapper;
    @Resource
    private CategoryService categoryService;

    @Override
    public IPage<BlogVo> blogList(BlogListSearchParams blogListSearchParams) {
        int page = blogListSearchParams.getPage() == null ? 1 : blogListSearchParams.getPage();
        int size = blogListSearchParams.getSize() == null ? 10 : blogListSearchParams.getSize();
        String sortBy = blogListSearchParams.getSortBy();
        String category = blogListSearchParams.getCategory();
        String status = blogListSearchParams.getStatus();
        String title = blogListSearchParams.getTitle();
        String keywords = blogListSearchParams.getKeywords();
        Date publishedStart = blogListSearchParams.getPublishedStart();
        Date publishedEnd = blogListSearchParams.getPublishedEnd();

        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        // 排序：默认根据时间排序
        if (StringUtils.isNotBlank(sortBy)) {
            if(sortBy.equals("publishedTime")){
                queryWrapper.orderByDesc(Blog::getPublishedTime);
            }else if (sortBy.equals("top")){
                queryWrapper.orderByDesc(Blog::getTop);
            }
        }
        // 筛选
        queryWrapper.eq(StringUtils.isNotBlank(status), Blog::getStatus, status);
        queryWrapper.eq(StringUtils.isNotBlank(category), Blog::getFatherCategoryId, category);
        queryWrapper.like(StringUtils.isNotBlank(title), Blog::getTitle, title);
        queryWrapper.like(StringUtils.isNotBlank(keywords),Blog::getDescription,keywords);
        if (publishedStart!= null && publishedEnd!= null) {
            queryWrapper.between(Blog::getPublishedTime, publishedStart, publishedEnd);
        } else if (publishedStart!= null) {
            queryWrapper.ge(Blog::getPublishedTime, publishedStart);
        } else if (publishedEnd!= null) {
            queryWrapper.le(Blog::getPublishedTime, publishedEnd);
        }

        // 分页
        Page<Blog> pageObj = new Page<>(page, size);
        List<Blog> blogList = blogMapper.selectList(pageObj, queryWrapper);

        // 获取文章分类信息
        List<BlogVo> blogVos = BeanCopyUtil.copyBeanList(blogList, BlogVo.class);
        for (BlogVo item : blogVos) {
            Category c = categoryService.getById(item.getCategoryId());
            item.setCategory(c);
        }

        //使用stream实现
        Page<BlogVo> pageVo = new Page<>();
        pageVo.setSize(pageObj.getSize());
        pageVo.setTotal(pageObj.getTotal());
        pageVo.setRecords(blogVos);
        pageVo.setPages(pageObj.getPages());
        pageVo.setCurrent(pageObj.getCurrent());

        return pageVo;
    };

    public BlogVo getBlogById(String id) {
        Blog blog = blogMapper.selectById(id);
        if(blog == null){
            return null;
        }
        BlogContent blogContent = blogContentMapper.selectById(id);
        BlogVo blogVo = BeanCopyUtil.copyBean(blog, BlogVo.class);
        if(blogContent != null){
            blogVo.setContent(blogContent.getContent());
        }
        return blogVo;
    }

    @Override
    public boolean removeById(String id) {
        blogMapper.deleteById(id);
        return true;
    }

    @Transactional
    @Override
    public boolean saveOrUpdate(BlogVo blogVo) {
        Blog blog = BeanCopyUtil.copyBean(blogVo, Blog.class);

        int res;
        if (blogVo.getId() == null) {
            blogMapper.insert(blog);
            // 获取插入后的文章id
            String id = blog.getId();
            BlogContent blogContent = new BlogContent(id, blogVo.getContent());
            res = blogContentMapper.insert(blogContent);
        }else {
            blogMapper.updateById(blog);
            String id = blog.getId();
            BlogContent blogContent = new BlogContent(id, blogVo.getContent());
            res = blogContentMapper.updateById(blogContent);
        }
        return res > 0;
    }
}
