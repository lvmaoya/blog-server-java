package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

        // 分页参数处理
        int pageNum = Optional.ofNullable(params.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(params.getSize()).orElse(10);

        Page<BlogVo> blogPage = blogMapper.selectBlogWithCategoryPage(
                new Page<>(pageNum, pageSize),
                params.getStatus(),
                params.getCategoryId(),
                params.getFatherCategoryId(),
                params.getTitle(),
                params.getKeywords(),
                params.getPublishedStart(),
                params.getPublishedEnd(),
                params.getSortBy(),
                params.getSortOrder()
        );

        return R.success(blogPage);
    }
    public R getBlogById(Integer id) {
        BlogVo blogVo = blogMapper.selectBlogWithContentById(id);
        if (blogVo == null) {
            return R.error(400,"博客不存在");
        }
        if (blogVo.getCategory() != null) {
            blogVo.setCategoryId(blogVo.getCategory().getId());//解决 @Results 映射配置中，categoryId 和 category.id 都映射到了同一个数据库列 category_id，这会导致映射冲突：
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
        asyncBlogService.updateBlog(blog.getId());
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
    @Transactional
    public R setDisable(String id) {
        Blog blog = blogMapper.selectById(id);
        // 取消置顶
        if (blog.getStatus() == 0) {
            blog.setStatus(1);
            blogMapper.updateById(blog);
            return R.success();
        }

        // 设置置顶
        blog.setStatus(0);
        blogMapper.updateById(blog);

        // 如果需要更复杂的置顶逻辑(如优先级)，可以在这里扩展
        return R.success();
    }

    @Override
    public R updateViewData(Integer id) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            return R.error(400, "没有此文章");
        }
        blog.setPageView(blog.getPageView() + 1);
        blogMapper.updateById(blog);
        return R.success();
    }
}
