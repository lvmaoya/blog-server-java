package com.lvmaoya.blog.blog.controller;

import com.lvmaoya.blog.blog.pojo.BlogForm;
import com.lvmaoya.blog.blog.pojo.BlogQuery;
import com.lvmaoya.blog.chat.service.RagAdminService;
import com.lvmaoya.blog.common.pojo.R;
import com.lvmaoya.blog.blog.service.BlogService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/blog")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BlogController {

    final BlogService blogService;
    final RagAdminService ragAdminService;

    @GetMapping("/list")
    public R list(BlogQuery blogQuery) {
        return blogService.blogList(blogQuery);
    }

    @GetMapping("/{id}")
    public R getArticle(@PathVariable Integer id) {
        return blogService.getBlogById(id);
    }

    @DeleteMapping("/{id}")
    public R deleteBlog(@PathVariable String id) {
        return blogService.removeById(id);
    }

    @PostMapping
    public R saveBlog(@RequestBody BlogForm blogVo) {
       return blogService.saveOrUpdate(blogVo);
    }

    @PutMapping("/{id}/top")
    public R setTop(@PathVariable String id) {
        return blogService.setTop(id);
    }

    @PutMapping("/{id}/disable")
    public R disable(@PathVariable String id) {
        return blogService.setDisable(id);
    }

    /**
     * 触发向量索引重建（全量）。
     * 路径：POST /blog/rag/reindex
     */
    @PostMapping("/rag/reindex")
    public R reindex(@RequestParam(name = "limit", required = false) Integer limit) {
        return ragAdminService.reindex(limit);
    }

    /**
     * 按博客ID增量重建向量索引。
     * 路径：POST /blog/rag/upsert?blogId=123
     */
    @PostMapping("/rag/upsert")
    public R upsert(@RequestParam(name = "blogId") Long blogId) {
        return ragAdminService.upsert(blogId);
    }

    /**
     * 分页检查 Milvus 中的数据；如传入 message 则进行语义相关查询并分页。
     * 路径：GET /blog/rag/inspect
     * 参数：page（默认1）、size（默认20）、message（可选）
     */
    @GetMapping("/rag/inspect")
    public R inspect(@RequestParam(name = "page", required = false) Integer page,
                     @RequestParam(name = "size", required = false) Integer size,
                     @RequestParam(name = "message", required = false) String message) {
        return ragAdminService.inspect(page, size, message);
    }
}
