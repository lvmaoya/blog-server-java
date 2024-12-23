package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.Result;
import com.lvmaoya.blog.domain.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Result<List<Category>> getCategoryList();
}
