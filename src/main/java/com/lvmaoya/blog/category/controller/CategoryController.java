package com.lvmaoya.blog.category.controller;

import com.lvmaoya.blog.category.entity.Category;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.category.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    CategoryService categoryService;

    @GetMapping("/list")
    public R getCategoryList() {
        return R.success(categoryService.list());
    }

    @PostMapping
    public R addOrUpdateCategory(@RequestBody Category category) {
        return categoryService.saveOrUpdateCategory(category);
    }

    @DeleteMapping("/{id}")
    public R deleteCategory(@PathVariable String id) {
        return categoryService.removeById(id);
    }
}
