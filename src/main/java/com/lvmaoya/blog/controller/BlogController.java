package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.service.BlogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;


    @GetMapping("/list")
    public Result<IPage<BlogVo>> list(@RequestBody(required = false) BlogListSearchParams blogListSearchParams) {
        return blogService.blogList(blogListSearchParams);
    }

    @GetMapping("/{id}")
    public Result<BlogVo> getArticle(@PathVariable String id) {
        return blogService.getBlogById(id);
    }


}
