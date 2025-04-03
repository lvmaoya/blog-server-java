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
import com.lvmaoya.blog.utils.EmailUtil;
import com.lvmaoya.blog.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
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
    private EmailUtil emailUtil;

    @Resource
    private IpUtils ipUtils;

    @Resource
    private BlogMapper blogMapper;

    @Override
    public R addOrUpdateComment(Comment comment, HttpServletRequest request) {
        Blog blog = blogMapper.selectById(comment.getArticleId());
        if (blog == null) {
            throw new BusinessException(400, "没有这篇文章");
        }
        // 构建邮件内容
        String subject = comment.getType() == 0 ?
                "LvmaoyaBlog - 您收到了新的文章评论" :
                "LvmaoyaBlog - 您收到了新的回复";

        String content;
        if (comment.getType() == 0) {
            // 文章评论邮件
            content = String.format(
                    "尊敬的作者，\n\n" +
                            "您的文章《%s》收到了一条新的评论：\n\n" +
                            "评论内容：\n%s\n\n" +
                            "评论人：%s\n" +
                            "评论时间：%s\n\n" +
                            "您可以点击以下链接查看详情：\n%s\n\n" +
                            "感谢您使用LvmaoyaBlog！\n\n" +
                            "此致\n敬礼\nLvmaoyaBlog团队",
                    blog.getTitle(),
                    comment.getContent(),
                    comment.getUserName(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(comment.getCreatedTime()),
                    "https://lvmaoya.cn/detail/" + comment.getArticleId()
            );
        } else {
            // 评论回复邮件
            Comment parentComment = commentMapper.selectById(comment.getRootId());
            content = String.format(
                    "尊敬的%s，\n\n" +
                            "您在文章《%s》中的评论收到了新的回复：\n\n" +
                            "您的原评论：\n%s\n\n" +
                            "回复内容：\n%s\n\n" +
                            "回复人：%s\n" +
                            "回复时间：%s\n\n" +
                            "您可以点击以下链接查看详情：\n%s\n\n" +
                            "感谢您使用LvmaoyaBlog！\n\n" +
                            "此致\n敬礼\nLvmaoyaBlog团队",
                    comment.getUserName(),
                    blog.getTitle(),
                    parentComment.getContent(),
                    comment.getContent(),
                    comment.getUserName(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(comment.getCreatedTime()),
                    "https://lvmaoya.cn/detail/" + comment.getArticleId()
            );
        }

        // 发送邮件（这里假设是发给管理员或文章作者）
        String toEmail = "admin@lvmaoya.com"; // 或从文章/用户信息中获取
        emailUtil.sendGeneralEmail(subject, content, toEmail);

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
