package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.entity.Category;
import com.lvmaoya.blog.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    CategoryService categoryService;

    @GetMapping("/list")
    public List<Category> getCategoryList() {
        return categoryService.list();
    }

    @PostMapping
    public boolean addCategory(@RequestBody Category category) {
        return categoryService.saveOrUpdate(category);
    }

    @DeleteMapping("/{id}")
    public boolean deleteCategory(@PathVariable String id) {
        return categoryService.removeById(id);
    }
}
