package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.dto.BlogPostDto;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.BlogService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/blog")
@PreAuthorize("hasRole('ADMIN')")
public class BlogController {
    @Resource
    private BlogService blogService;

    @GetMapping("/list")
    public R list(@ModelAttribute BlogListSearchParams blogListSearchParams) {
        return blogService.blogList(blogListSearchParams);
    }

    @GetMapping("/{id}")
    public R getArticle(@PathVariable String id) {
        return blogService.getBlogById(id);
    }

    @DeleteMapping("/{id}")
    public R deleteBlog(@PathVariable String id) {
        return blogService.removeById(id);
    }

    @PostMapping
    public R saveBlog(@RequestBody BlogPostDto blogVo) {
       return blogService.saveOrUpdate(blogVo);
    }

    @PutMapping("/{id}/top")
    public R setTop(@PathVariable String id) {
        return blogService.setTop(id);
    }

    @PutMapping("/{id}/disable")
    public R disable(@PathVariable String id) {
        return blogService.setDisable(id);
    }
}
