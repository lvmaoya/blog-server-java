package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.service.BlogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    /**
     * 查询文章数据
     *
     * @param blogListSearchParams 实体对象
     * @return 查询结果
     */
    @PostMapping("/list")
    public IPage<BlogVo> list(@RequestBody(required = false) BlogListSearchParams blogListSearchParams) {
        return blogService.blogList(blogListSearchParams);
    }

    @GetMapping("/{id}")
    public BlogVo getArticle(@PathVariable String id) {
        return blogService.getBlogById(id);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteBlog(@PathVariable String id) {
        return blogService.removeById(id);
    }

    /**
     * 新增文章数据
     *
     * @param blogVo 实体对象
     * @return 新增结果
     */
    @PostMapping
    public boolean saveBlog(@RequestBody BlogVo blogVo) {
       return blogService.saveOrUpdate(blogVo);
    }
}
