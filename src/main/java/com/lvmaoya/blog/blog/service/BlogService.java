package com.lvmaoya.blog.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.blog.pojo.BlogForm;
import com.lvmaoya.blog.blog.entity.Blog;
import com.lvmaoya.blog.blog.entity.BlogContent;
import com.lvmaoya.blog.blog.pojo.BlogQuery;
import com.lvmaoya.blog.blog.pojo.BlogVo;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.blog.mapper.BlogMapper;
import com.lvmaoya.blog.category.service.CategoryService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import com.lvmaoya.blog.chat.service.RagVectorIndexService;
import org.springframework.context.ApplicationEventPublisher;
import com.lvmaoya.blog.event.BlogSavedEvent;
import com.lvmaoya.blog.event.BlogDeletedEvent;

@Service
public class BlogService extends ServiceImpl<BlogMapper, Blog> {
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private BlogContentMapper blogContentMapper;
    @Resource
    private CategoryService categoryService;
    @Resource
    private AsyncBlogService asyncBlogService;
    @Resource
    private RagVectorIndexService ragVectorIndexService;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    public R blogList(BlogQuery params) {

        // 分页参数处理
        int pageNum = Optional.ofNullable(params.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(params.getSize()).orElse(10);

        Page<BlogVo> blogPage = blogMapper.selectBlogWithCategoryPage(
                new Page<>(pageNum, pageSize),
                params.getStatus(),
                params.getCategoryId(),
                params.getFatherCategoryId(),
                params.getFatherCategoryIds(),
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

    public R removeById(String id) {
        blogMapper.deleteById(id);
        try {
            // 发布删除事件，在事务提交后异步清理向量索引
            eventPublisher.publishEvent(new BlogDeletedEvent(Integer.valueOf(id)));
        } catch (Exception ignored) {}
        return R.success(true);
    }

    @Transactional
    public R saveOrUpdate(BlogForm blogVo) {
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
        // 发布事件：在事务提交后由监听器异步处理（摘要生成与向量索引重建）
        eventPublisher.publishEvent(new BlogSavedEvent(blog.getId(), Boolean.TRUE.equals(blogVo.getKeepDesc())));
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
