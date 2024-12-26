package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.service.BlogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @GetMapping("/list")
    public Result<IPage<BlogVo>> list(@RequestBody(required = false) Blog blog, @RequestBody(required = false)  Integer page, @RequestBody(required = false)  Integer size, @RequestBody(required = false)  String sortBy) {
        return blogService.blogList(page,size,blog,sortBy);
    }

    @GetMapping("/{id}")
    public Result<BlogVo> getArticle(@PathVariable String id) {
        return blogService.getBlogById(id);
    }


}
