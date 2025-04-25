package com.lvmaoya.blog.h5Controller;

import com.lvmaoya.blog.domain.dto.BlogPostDto;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.service.CategoryService;
import com.lvmaoya.blog.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/h5")
public class H5BlogController {
    @Resource
    private BlogService blogService;

    @Resource
    private UserService userService;
    @Resource
    private CategoryService categoryService;
    @GetMapping("/user")
    public R<UserVo> getUserDetail(){
        return R.success(userService.getUser(1));
    }

    @GetMapping("/category/list")
    public R getCategoryList() {
        return R.success(categoryService.list());
    }

    @PostMapping("/blog/list")
    public R list(@RequestBody(required = false) BlogListSearchParams blogListSearchParams) {
        return blogService.blogList(blogListSearchParams);
    }

    @GetMapping("/blog/{id}")
    public R getArticle(@PathVariable String id) {
        return blogService.getBlogById(id);
    }

}
