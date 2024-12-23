package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.Result;
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
    public Result<IPage<BlogVo>> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size, Integer status, Integer top) {
        return blogService.blogList(page,size,status,top);
    }

    @GetMapping("/{id}")
    public Result<BlogVo> getArticle(@PathVariable String id) {
        return blogService.getBlogById(id);
    }
}
