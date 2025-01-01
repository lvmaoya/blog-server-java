package com.lvmaoya.blog.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping
    public boolean postComment(@RequestBody Comment comment) {
        return commentService.addOrUpdateComment(comment);
    }
    @PostMapping("/list")
    public IPage<Comment> getCommentList(@RequestBody CommentSearchParams commentSearchParams) {
        return commentService.selectList(commentSearchParams);
    }
    @DeleteMapping("/{id}")
    public boolean deleteComment(@PathVariable String id) {
        return commentService.removeById(id);
    }
}
