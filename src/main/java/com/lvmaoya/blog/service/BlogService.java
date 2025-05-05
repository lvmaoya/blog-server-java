package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.dto.BlogPostDto;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.domain.vo.R;

public interface BlogService extends IService<Blog> {
    R blogList(BlogListSearchParams blogListSearchParams);
    R getBlogById(Integer id);
    R removeById(String id);
    R saveOrUpdate(BlogPostDto blogVo);

    R setTop(String id);

    R setDisable(String id);

    R updateViewData(Integer id);
}
