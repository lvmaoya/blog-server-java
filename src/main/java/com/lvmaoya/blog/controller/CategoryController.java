package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    CategoryService categoryService;

    @GetMapping("/list")
    public List<Category> getCategoryList() {
        return categoryService.getCategoryList();
    }

}
