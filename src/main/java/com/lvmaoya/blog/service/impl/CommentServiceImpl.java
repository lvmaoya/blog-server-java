package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.handler.exception.BusinessException;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.mapper.CommentMapper;
import com.lvmaoya.blog.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private BlogMapper blogMapper;

    @Override
    public R addOrUpdateComment(Comment comment) {
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("id", comment.getArticleId());
        Long l = blogMapper.selectCount(blogQueryWrapper);
        if (l == 0) {
            throw new BusinessException(400, "没有这篇文章");
        }
        return R.success(commentMapper.insertOrUpdate(comment));
    }

    @Override
    public R selectList(CommentSearchParams commentSearchParams) {
        int page = commentSearchParams.getPage() == null ? 1 : commentSearchParams.getPage();
        int size = commentSearchParams.getSize() == null ? 20 : commentSearchParams.getSize();

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(commentSearchParams.getArticleId()),Comment::getArticleId, commentSearchParams.getArticleId());
        queryWrapper.eq(Objects.nonNull(commentSearchParams.getStatus()),Comment::getStatus, commentSearchParams.getStatus());

        IPage<Comment> iPage = new Page<>(page,size);
        return R.success(commentMapper.selectPage(iPage,queryWrapper));
    }
}
