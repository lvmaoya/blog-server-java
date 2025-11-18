package com.lvmaoya.blog.comment.controller;
import com.lvmaoya.blog.comment.pojo.CommentForm;
import com.lvmaoya.blog.comment.pojo.CommentStatusUpdateForm;
import com.lvmaoya.blog.comment.pojo.CommentQuery;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.comment.service.CommentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping
    public R addOrUpdateComment(@RequestBody CommentForm commentForm, HttpServletRequest request) {
        return commentService.addOrUpdateComment(commentForm, request);
    }
    @GetMapping("/list")
    public R getCommentList(@ModelAttribute CommentQuery commentQuery) {
        return commentService.selectList(commentQuery);
    }
    @DeleteMapping("/{id}")
    public R deleteComment(@PathVariable String id) {
        return R.success(commentService.removeById(id));
    }

    @PostMapping("/updateStatus")
    public R updateStatus(@RequestBody CommentStatusUpdateForm dto) {
        return commentService.updateCommentStatus(dto);
    }
}
