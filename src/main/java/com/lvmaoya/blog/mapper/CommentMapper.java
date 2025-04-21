package com.lvmaoya.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.entity.Comment;
import com.lvmaoya.blog.domain.searchParams.CommentSearchParams;
import com.lvmaoya.blog.domain.vo.CommentVo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("<script>" +
            "SELECT c.*, b.title AS article_title, u.username, u.avatar, u.email, u.site " +
            "FROM comment c " +
            "LEFT JOIN blog b ON c.article_id = b.id AND b.deleted = 0 " +
            "LEFT JOIN comment_user u ON c.user_id = u.id " +
            "<where>" +
            "   <if test='params.articleId != null'> AND c.article_id = #{params.articleId} </if>" +
            "   <if test='params.status != null'> AND c.status = #{params.status} </if>" +
            "</where>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time"),
            // 其他字段映射...
            @Result(property = "articleTitle", column = "article_title"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "email", column = "email"),
            @Result(property = "site", column = "site")
    })
    IPage<CommentVo> selectCommentPage(IPage<Comment> page, @Param("params") CommentSearchParams params);
}
