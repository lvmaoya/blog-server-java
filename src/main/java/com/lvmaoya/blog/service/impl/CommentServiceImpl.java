package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.CommentVo;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.handler.exception.BusinessException;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.mapper.CommentMapper;
import com.lvmaoya.blog.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        IPage<Comment> commentPage = commentMapper.selectPage(iPage, queryWrapper);
        // 提取所有文章ID
        Set<Integer> articleIds = commentPage.getRecords().stream()
                .map(Comment::getArticleId)
                .collect(Collectors.toSet());

        // 批量查询文章
        Map<Integer, String> articleTitleMap = blogMapper.selectBatchIds(articleIds)
                .stream()
                .collect(Collectors.toMap(Blog::getId, Blog::getTitle));

        // 转换为DTO
        List<CommentVo> vos = commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVo vo = new CommentVo();
                    BeanUtils.copyProperties(comment, vo);
                    vo.setArticleTitle(articleTitleMap.get(comment.getArticleId()));
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建返回分页对象
        Page<CommentVo> resultPage = new Page<>(
                commentPage.getCurrent(),
                commentPage.getSize(),
                commentPage.getTotal()
        );
        resultPage.setRecords(vos);

        return R.success(resultPage);
    }
}
