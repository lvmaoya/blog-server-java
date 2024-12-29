package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;

public interface BlogService extends IService<Blog> {
    IPage<BlogVo> blogList(BlogListSearchParams blogListSearchParams);
    BlogVo getBlogById(String id);
    boolean removeById(String id);
    boolean saveOrUpdate(BlogVo blogVo);
}
