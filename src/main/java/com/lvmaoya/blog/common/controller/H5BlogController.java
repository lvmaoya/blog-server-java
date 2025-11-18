package com.lvmaoya.blog.common.controller;

import com.lvmaoya.blog.blog.service.BlogService;
import com.lvmaoya.blog.blog.service.OnlineService;
import com.lvmaoya.blog.category.service.CategoryService;
import com.lvmaoya.blog.comment.service.CommentService;
import com.lvmaoya.blog.category.pojo.CategoryQuery;
import com.lvmaoya.blog.comment.pojo.CommentForm;
import com.lvmaoya.blog.blog.pojo.BlogQuery;
import com.lvmaoya.blog.comment.pojo.CommentQuery;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.user.pojo.UserVo;
import com.lvmaoya.blog.user.service.UserService;
import com.lvmaoya.blog.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;


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
    private OnlineService onlineService;

    @Resource
    private CommentService commentService;
    @GetMapping("/user")
    public R<UserVo> getUserDetail(){
        return R.success(userService.getUser(1));
    }

    @GetMapping("/category/list")
    public R getCategoryList(@ModelAttribute CategoryQuery categoryQuery) {
        return categoryService.getCategoryListWithCount(categoryQuery);
    }

    @GetMapping("/blog/list")
    public R list(@ModelAttribute BlogQuery blogQuery) {
        blogQuery.setStatus("1");
        return blogService.blogList(blogQuery);
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
    public R getCommentList(@ModelAttribute CommentQuery commentQuery) {
        return commentService.selectList(commentQuery);
    }
    @PostMapping("/comment")
    public R addOrUpdateComment(@RequestBody CommentForm commentForm, HttpServletRequest request) {
        return commentService.addOrUpdateComment(commentForm, request);
    }

    @GetMapping("/heart")
    public String heartbeat(HttpServletRequest request) {
        onlineService.heartbeat(IpUtils.getClientIp(request));
        return "success";
    }
}
