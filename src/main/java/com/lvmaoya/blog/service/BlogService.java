package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.vo.BlogVo;

public interface BlogService extends IService<Blog> {
    Result<IPage<BlogVo>> blogList(Integer page, Integer size, Blog blog, String sortBy);
    Result<BlogVo> getBlogById(String id);
}
