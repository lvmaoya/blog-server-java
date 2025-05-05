package com.lvmaoya.blog.h5Controller;

import com.lvmaoya.blog.domain.dto.BlogPostDto;
import com.lvmaoya.blog.domain.dto.CategoryGetDto;
import com.lvmaoya.blog.domain.dto.CommentPostDto;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.domain.vo.UserVo;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.service.CategoryService;
import com.lvmaoya.blog.service.CommentService;
import com.lvmaoya.blog.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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

    @Resource
    private CommentService commentService;
    @GetMapping("/user")
    public R<UserVo> getUserDetail(){
        return R.success(userService.getUser(1));
    }

    @GetMapping("/category/list")
    public R getCategoryList(@ModelAttribute CategoryGetDto categoryGetDto) {
        return categoryService.getCategoryList(categoryGetDto);
    }

    @GetMapping("/blog/list")
    public R list(@ModelAttribute BlogListSearchParams blogListSearchParams) {
        return blogService.blogList(blogListSearchParams);
    }

    @GetMapping("/blog/{id}")
    public R getArticle(@PathVariable Integer id) {
        return blogService.getBlogById(id);
    }
    @GetMapping("/blog/{id}/view")
    public R viewArticle(@PathVariable Integer id) {
        return blogService.updateViewData(id);
    }
    @GetMapping("/comment/list")
    public R getCommentList(@ModelAttribute CommentSearchParams commentSearchParams) {
        return commentService.selectList(commentSearchParams);
    }
    @PostMapping("/comment")
    public R addOrUpdateComment(@RequestBody CommentPostDto commentPostDto, HttpServletRequest request) {
        return commentService.addOrUpdateComment(commentPostDto, request);
    }
}
