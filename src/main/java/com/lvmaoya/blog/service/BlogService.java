package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;

import java.lang.reflect.Array;
import java.time.LocalDateTime;

public interface BlogService extends IService<Blog> {
    Result<IPage<BlogVo>> blogList(BlogListSearchParams blogListSearchParams);
    Result<BlogVo> getBlogById(String id);
}
