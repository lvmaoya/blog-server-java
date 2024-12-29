package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> getCategoryList();
}
