package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


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
