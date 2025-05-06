package com.lvmaoya.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lvmaoya.blog.domain.dto.CategoryGetDto;
import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.vo.R;

import java.util.List;

public interface CategoryService extends IService<Category> {
    R removeById(String id);
    R saveOrUpdateCategory(Category category);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Integer id);

    R getCategoryList(CategoryGetDto categoryGetDto);
    R getCategoryListWithCount(CategoryGetDto categoryGetDto);
}
