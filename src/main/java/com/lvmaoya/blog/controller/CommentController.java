package com.lvmaoya.blog.controller;

import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping
    public boolean postComment(@RequestBody Comment comment) {
        return commentService.addOrUpdateComment();
    }
    @PostMapping
    public boolean getCommentList(@RequestBody CommentSearchParams commentSearchParams) {
        return commentService.selectList();
    }
    @DeleteMapping("/{id}")
    public boolean deleteComment(@PathVariable String id) {
        return commentService.removeById(id);
    }
    @PostMapping("/like/{id}")
    public boolean likeComment(@PathVariable String id) {
        return commentService.updateComment();
    }
    @PostMapping("/unlike/{id}")
    public boolean likeComment(@PathVariable String id) {
        return commentService.updateComment();
    }
}
