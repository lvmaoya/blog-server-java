package com.lvmaoya.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lvmaoya.blog.domain.dto.CommentPostDto;
import com.lvmaoya.blog.domain.dto.UpdateCommentStatusDto;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.entity.CommentUser;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.CommentVo;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.handler.exception.BusinessException;
import com.lvmaoya.blog.mapper.BlogMapper;
import com.lvmaoya.blog.mapper.CommentMapper;
import com.lvmaoya.blog.mapper.CommentUserMapper;
import com.lvmaoya.blog.service.CommentService;
import com.lvmaoya.blog.utils.BeanCopyUtil;
import com.lvmaoya.blog.utils.EmailUtil;
import com.lvmaoya.blog.utils.IpUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
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
    @Resource
    private CommentUserMapper commentUserMapper;
    @Override
    @Transactional
    public R addOrUpdateComment(CommentPostDto commentPostDto, HttpServletRequest request) {
        Blog blog = blogMapper.selectById(commentPostDto.getArticleId());
        if (blog == null) {
            throw new BusinessException(400, "没有这篇文章");
        }
        // 获取用户的ip，到用户库里查询，如果没有该用户且该用户的name没填需要抛出异常
        String clientIp = ipUtils.getClientIp(request);
        CommentUser commentUser = commentUserMapper.selectById(clientIp);
        if(Objects.isNull(commentUser) && Objects.isNull(commentPostDto.getUsername())){
            throw new BusinessException(400, "用户名必填");
        }
        CommentUser commentUserInsert = BeanCopyUtil.copyBean(commentPostDto, CommentUser.class);
        commentUserInsert.setId(clientIp);
        commentUserMapper.insertOrUpdate(commentUserInsert);

        // 构建邮件内容
        String subject = commentPostDto.getType() == 0 ?
                "LvmaoyaBlog - 您收到了新的文章评论" :
                "LvmaoyaBlog - 您收到了新的回复";

        String content;
        String toEmail;
        if (commentPostDto.getType() == 0) {
            // 文章评论邮件
            content = String.format(
                    "尊敬的作者，\n\n" +
                            "您的文章《%s》收到了一条新的评论：\n\n" +
                            "评论内容：\n%s\n\n" +
                            "评论人：%s\n\n" +
                            "评论时间：%s\n\n" +
                            "您可以点击以下链接查看详情：\n%s\n\n" +
                            "感谢您使用LvmaoyaBlog！\n\n" +
                            "此致\n敬礼\nLvmaoyaBlog团队",
                    blog.getTitle(),
                    commentPostDto.getContent(),
                    commentPostDto.getUsername(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    "https://lvmaoya.cn/detail/" + commentPostDto.getArticleId()
            );
            toEmail = "1504734652@qq.com";
        } else {
            // 评论回复的那个邮件
            Comment toComment = commentMapper.selectById(commentPostDto.getToCommentId());
            if (Objects.isNull(toComment)){
                throw new BusinessException(400, "未找到评论对象");
            }
            CommentUser toCommentUser = commentUserMapper.selectById(toComment.getUserId());

            content = String.format(
                    "尊敬的%s，\n\n" +
                            "您在文章《%s》中的评论收到了新的回复：\n\n" +
                            "您的原评论：\n%s\n\n" +
                            "回复内容：\n%s\n\n" +
                            "回复时间：%s\n\n" +
                            "您可以点击以下链接查看详情：\n%s\n\n" +
                            "感谢您使用LvmaoyaBlog！\n\n" +
                            "此致\n敬礼\nLvmaoyaBlog团队",
                    toCommentUser.getUsername(),
                    blog.getTitle(),
                    toComment.getContent(),
                    commentPostDto.getContent(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    "https://lvmaoya.cn/detail/" + commentPostDto.getArticleId()
            );
            toEmail = toCommentUser.getEmail();
        }
        Comment comment = BeanCopyUtil.copyBean(commentPostDto, Comment.class);
        comment.setUserId(clientIp);
        commentMapper.insertOrUpdate(comment);

        emailUtil.sendGeneralEmail(subject, content, toEmail);
        return R.success();
    }

    @Override
    public R selectList(CommentSearchParams commentSearchParams) {
        int page = commentSearchParams.getPage() == null ? 1 : commentSearchParams.getPage();
        int size = commentSearchParams.getSize() == null ? 20 : commentSearchParams.getSize();

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(commentSearchParams.getArticleId()), Comment::getArticleId, commentSearchParams.getArticleId());
        queryWrapper.eq(Objects.nonNull(commentSearchParams.getStatus()), Comment::getStatus, commentSearchParams.getStatus());

        IPage<Comment> iPage = new Page<>(page, size);
        IPage<Comment> commentPage = commentMapper.selectPage(iPage, queryWrapper);

        // Extract all article IDs
        Set<Integer> articleIds = commentPage.getRecords().stream()
                .map(Comment::getArticleId)
                .collect(Collectors.toSet());

        // Extract all user IDs (assuming Comment has a userId field)
        Set<String> userIds = commentPage.getRecords().stream()
                .map(Comment::getUserId) // Make sure Comment has this field
                .collect(Collectors.toSet());

        // Batch query articles
        Map<Integer, String> articleTitleMap = blogMapper.selectBatchIds(articleIds)
                .stream()
                .collect(Collectors.toMap(Blog::getId, Blog::getTitle));

        // Batch query users
        Map<String, CommentUser> userMap = commentUserMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(CommentUser::getId, user -> user));

        // Convert to DTO
        List<CommentVo> vos = commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVo vo = new CommentVo();
                    BeanUtils.copyProperties(comment, vo);
                    vo.setArticleTitle(articleTitleMap.get(comment.getArticleId()));

                    // Set user information
                    CommentUser user = userMap.get(comment.getUserId());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                        vo.setAvatar(user.getAvatar());
                        vo.setEmail(user.getEmail());
                        vo.setSite(user.getSite());
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // Build return page object
        Page<CommentVo> resultPage = new Page<>(
                commentPage.getCurrent(),
                commentPage.getSize(),
                commentPage.getTotal()
        );
        resultPage.setRecords(vos);

        return R.success(resultPage);
    }

    @Override
    public R updateCommentStatus(UpdateCommentStatusDto dto) {
        // Check if comment exists
        Comment comment = commentMapper.selectById(dto.getCommentId());
        if (comment == null) {
            return R.error(400,"评论不存在");
        }

        // Update status
        comment.setStatus(dto.getStatus());
        int result = commentMapper.updateById(comment);

        if (result > 0) {
            return R.success("状态更新成功");
        } else {
            return R.error(400,"状态更新失败");
        }
    }
}
