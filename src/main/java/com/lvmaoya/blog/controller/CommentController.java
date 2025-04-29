package com.lvmaoya.blog.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.dto.CommentPostDto;
import com.lvmaoya.blog.domain.dto.UpdateCommentStatusDto;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.CommentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping
    public R addOrUpdateComment(@RequestBody CommentPostDto commentPostDto, HttpServletRequest request) {
        return commentService.addOrUpdateComment(commentPostDto, request);
    }
    @GetMapping("/list")
    public R getCommentList(@ModelAttribute CommentSearchParams commentSearchParams) {
        return commentService.selectList(commentSearchParams);
    }
    @DeleteMapping("/{id}")
    public R deleteComment(@PathVariable String id) {
        return R.success(commentService.removeById(id));
    }

    @PostMapping("/updateStatus")
    public R updateStatus(@RequestBody UpdateCommentStatusDto dto) {
        return commentService.updateCommentStatus(dto);
    }
}
