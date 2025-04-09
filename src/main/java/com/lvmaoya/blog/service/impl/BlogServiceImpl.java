package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.dto.BlogPostDto;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.BlogContent;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.service.AsyncBlogService;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.service.CategoryService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.chat.prompt.Prompt;

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
    @Resource
    private AsyncBlogService asyncBlogService;
    @Override
    public R blogList(BlogListSearchParams blogListSearchParams) {
        int page = blogListSearchParams.getPage() == null ? 1 : blogListSearchParams.getPage();
        int size = blogListSearchParams.getSize() == null ? 10 : blogListSearchParams.getSize();
        String sortBy = blogListSearchParams.getSortBy();
        String sortOrder = blogListSearchParams.getSortOrder(); // 新增的排序方向参数
        String category = blogListSearchParams.getCategory();
        String status = blogListSearchParams.getStatus();
        String title = blogListSearchParams.getTitle();
        String keywords = blogListSearchParams.getKeywords();
        Date publishedStart = blogListSearchParams.getPublishedStart();
        Date publishedEnd = blogListSearchParams.getPublishedEnd();

        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序逻辑（修改后）
        if (StringUtils.isNotBlank(sortBy)) {
            boolean isAsc = StringUtils.isNotBlank(sortOrder) && "asc".equalsIgnoreCase(sortOrder);

            if (sortBy.equals("publishedTime")) {
                if (isAsc) {
                    queryWrapper.orderByAsc(Blog::getPublishedTime);
                } else {
                    queryWrapper.orderByDesc(Blog::getPublishedTime); // 默认降序
                }
            } else if (sortBy.equals("top")) {
                if (isAsc) {
                    queryWrapper.orderByAsc(Blog::getTop);
                } else {
                    queryWrapper.orderByDesc(Blog::getTop); // 默认降序
                }
            }
            // 可以继续添加其他排序字段...
        } else {
            // 默认排序（如果没有指定排序字段）
            queryWrapper.orderByDesc(Blog::getPublishedTime);
        }

        // 原有筛选条件保持不变...
        queryWrapper.eq(StringUtils.isNotBlank(status), Blog::getStatus, status);
        queryWrapper.eq(StringUtils.isNotBlank(category), Blog::getFatherCategoryId, category);
        queryWrapper.like(StringUtils.isNotBlank(title), Blog::getTitle, title);
        queryWrapper.like(StringUtils.isNotBlank(keywords), Blog::getDescription, keywords);

        if (publishedStart != null && publishedEnd != null) {
            queryWrapper.between(Blog::getPublishedTime, publishedStart, publishedEnd);
        } else if (publishedStart != null) {
            queryWrapper.ge(Blog::getPublishedTime, publishedStart);
        } else if (publishedEnd != null) {
            queryWrapper.le(Blog::getPublishedTime, publishedEnd);
        }

        // 分页查询
        Page<Blog> pageObj = new Page<>(page, size);
        IPage<Blog> blogPage = blogMapper.selectPage(pageObj, queryWrapper);

        // 转换VO并设置分类信息
        List<BlogVo> blogVos = BeanCopyUtil.copyBeanList(blogPage.getRecords(), BlogVo.class);
        for (BlogVo item : blogVos) {
            Category c = categoryService.getById(item.getCategoryId());
            item.setCategory(c);
        }

        // 构建返回的分页对象
        Page<BlogVo> pageVo = new Page<>();
        pageVo.setSize(blogPage.getSize());
        pageVo.setTotal(blogPage.getTotal());
        pageVo.setRecords(blogVos);
        pageVo.setPages(blogPage.getPages());
        pageVo.setCurrent(blogPage.getCurrent());

        return R.success(pageVo);
    }
    public R getBlogById(String id) {
        Blog blog = blogMapper.selectById(id);
        if(blog == null){
            return null;
        }
        BlogContent blogContent = blogContentMapper.selectById(id);
        BlogVo blogVo = BeanCopyUtil.copyBean(blog, BlogVo.class);
        if(blogContent != null){
            blogVo.setContent(blogContent.getContent());
        }
        return R.success(blogVo);
    }

    @Override
    public R removeById(String id) {
        blogMapper.deleteById(id);
        return R.success(true);
    }

    @Transactional
    @Override
    public R saveOrUpdate(BlogPostDto blogVo) {
        Blog blog = BeanCopyUtil.copyBean(blogVo, Blog.class);

        int res;
        if (blogVo.getId() == null) {
            blogMapper.insert(blog);
            // 获取插入后的文章id
            Integer id = blog.getId();
            BlogContent blogContent = new BlogContent();
            blogContent.setContent(blogVo.getContent());
            blogContent.setId(id);
            res = blogContentMapper.insert(blogContent);
        }else {
            blogMapper.updateById(blog);
            Integer id = blog.getId();
            BlogContent blogContent = new BlogContent();
            blogContent.setContent(blogVo.getContent());
            blogContent.setId(id);
            res = blogContentMapper.updateById(blogContent);
        }
        // 异步生成摘要
        // Spring的@Async是基于代理实现的，同一个类内部的方法调用不会经过代理，导致异步失效。
//        asyncBlogService.updateBlog(blog.getId());
        return R.success(res > 0);
    }
    @Transactional
    public R setTop(String id) {
        Blog blog = blogMapper.selectById(id);
        // 取消置顶
        if (blog.getTop() == 0) {
            blog.setTop(1);
            blogMapper.updateById(blog);
            return R.success();
        }

        // 设置置顶
        blog.setTop(0);
        blogMapper.updateById(blog);

        // 如果需要更复杂的置顶逻辑(如优先级)，可以在这里扩展
        return R.success();
    }
}
