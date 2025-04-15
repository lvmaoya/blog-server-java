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
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public R blogList(BlogListSearchParams params) {
        // 1. 分页参数处理
        int page = Optional.ofNullable(params.getPage()).orElse(1);
        int size = Optional.ofNullable(params.getSize()).orElse(10);

        // 2. 构建查询条件
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序逻辑
        applySorting(queryWrapper, params.getSortBy(), params.getSortOrder());

        // 筛选条件
        applyFilters(queryWrapper, params);

        // 3. 执行分页查询
        IPage<Blog> blogPage = blogMapper.selectPage(new Page<>(page, size), queryWrapper);

        // 4. 转换结果
        Page<BlogVo> resultPage = convertToVoPage(blogPage);

        return R.success(resultPage);
    }

    // 排序逻辑抽取
    private void applySorting(LambdaQueryWrapper<Blog> wrapper, String sortBy, String sortOrder) {
        if (StringUtils.isNotBlank(sortBy)) {
            boolean isAsc = StringUtils.isNotBlank(sortOrder) && "asc".equalsIgnoreCase(sortOrder);
            switch (sortBy) {
                case "publishedTime":
                    wrapper.orderBy(isAsc, true, Blog::getPublishedTime);
                    break;
                case "top":
                    wrapper.orderBy(isAsc, true, Blog::getTop);
                    break;
                default:
                    wrapper.orderByDesc(Blog::getPublishedTime);
            }
        } else {
            wrapper.orderByDesc(Blog::getPublishedTime);
        }
    }

    // 筛选条件抽取
    private void applyFilters(LambdaQueryWrapper<Blog> wrapper, BlogListSearchParams params) {
        wrapper.eq(StringUtils.isNotBlank(params.getStatus()), Blog::getStatus, params.getStatus())
                .eq(StringUtils.isNotBlank(params.getCategory()), Blog::getFatherCategoryId, params.getCategory())
                .like(StringUtils.isNotBlank(params.getTitle()), Blog::getTitle, params.getTitle())
                .like(StringUtils.isNotBlank(params.getKeywords()), Blog::getDescription, params.getKeywords());

        if (params.getPublishedStart() != null || params.getPublishedEnd() != null) {
            wrapper.between(params.getPublishedStart() != null && params.getPublishedEnd() != null,
                            Blog::getPublishedTime,
                            params.getPublishedStart(),
                            params.getPublishedEnd())
                    .ge(params.getPublishedStart() != null && params.getPublishedEnd() == null,
                            Blog::getPublishedTime,
                            params.getPublishedStart())
                    .le(params.getPublishedEnd() != null && params.getPublishedStart() == null,
                            Blog::getPublishedTime,
                            params.getPublishedEnd());
        }
    }

    // 结果转换抽取
    private Page<BlogVo> convertToVoPage(IPage<Blog> blogPage) {
        List<BlogVo> blogVos = blogPage.getRecords().stream()
                .map(blog -> {
                    BlogVo vo = BeanCopyUtil.copyBean(blog, BlogVo.class);
                    vo.setCategory(categoryService.getById(blog.getCategoryId()));
                    return vo;
                })
                .collect(Collectors.toList());

        Page<BlogVo> pageVo = new Page<>();
        BeanUtils.copyProperties(blogPage, pageVo, "records");
        pageVo.setRecords(blogVos);
        return pageVo;
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
