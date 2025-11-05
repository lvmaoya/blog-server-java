package com.lvmaoya.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lvmaoya.blog.domain.dto.BlogPostDto;
import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.searchParams.BlogListSearchParams;
import com.lvmaoya.blog.domain.vo.BlogVo;
import com.lvmaoya.blog.domain.vo.R;
import com.lvmaoya.blog.service.BlogService;
import com.lvmaoya.blog.service.rag.RagVectorIndexService;
import com.lvmaoya.blog.service.rag.RagVectorSearchService;
import com.lvmaoya.blog.service.rag.ZhipuEmbeddingService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.response.QueryResultsWrapper;
import org.springframework.core.env.Environment;

import java.util.*;


@RestController
@RequestMapping("/blog")
@PreAuthorize("hasRole('ADMIN')")
public class BlogController {
    @Resource
    private BlogService blogService;
    @Resource
    private RagVectorIndexService ragVectorIndexService;
    @Resource
    private RagVectorSearchService ragVectorSearchService;
    @Resource
    private MilvusClientV2 milvusClient;
    @Resource
    private ZhipuEmbeddingService zhipuEmbeddingService;
    @Resource
    private Environment env;

    @GetMapping("/list")
    public R list(@ModelAttribute BlogListSearchParams blogListSearchParams) {
        return blogService.blogList(blogListSearchParams);
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
    public R saveBlog(@RequestBody BlogPostDto blogVo) {
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
        try {
            ragVectorIndexService.rebuildVectorIndex(limit);
            String msg = (limit != null && limit > 0) ? ("重建完成（仅处理前" + limit + "篇）") : "重建完成";
            return R.success(msg);
        } catch (Exception e) {
            return R.error(2000, "重建失败: " + e.getMessage());
        }
    }

    /**
     * 按博客ID增量重建向量索引。
     * 路径：POST /blog/rag/upsert?blogId=123
     */
    @PostMapping("/rag/upsert")
    public R upsert(@RequestParam(name = "blogId") Long blogId) {
        try {
            ragVectorIndexService.upsertBlog(blogId);
            return R.success("已重建该文章向量索引");
        } catch (Exception e) {
            return R.error(2001, "重建失败: " + e.getMessage());
        }
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
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : size;

        String coll = Optional.ofNullable(env.getProperty("rag.collection.name")).orElse("blog_chunks");
        try {
            milvusClient.loadCollection(LoadCollectionReq.builder().collectionName(coll).build());
        } catch (Exception e) {
            return R.error(2002, "加载集合失败: " + e.getMessage());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("page", pageNum);
        payload.put("size", pageSize);

        // 若传入 message，使用语义检索并进行分页切片
        if (message != null && !message.trim().isEmpty()) {
            try {
                java.util.List<com.lvmaoya.blog.service.rag.RagVectorSearchService.SearchHit> all =
                        ragVectorSearchService.searchBySemantic(message);
                int total = all.size();
                int start = (pageNum - 1) * pageSize;
                int end = Math.min(total, start + pageSize);
                List<RagVectorSearchService.SearchHit> items =
                        (start >= total) ? Collections.emptyList() : all.subList(start, end);
                payload.put("total", total);
                payload.put("items", items);
                return R.success(payload);
            } catch (Exception e) {
                return R.error(2003, "语义查询失败: " + e.getMessage());
            }
        }

        // 未传入 message，执行 Milvus 标量查询分页
        try {
            QueryReq.QueryReqBuilder builder = QueryReq.builder()
                    .collectionName(coll)
                    .outputFields(Arrays.asList("blog_id", "chunk_index", "title", "content_preview"))
                    .limit(pageSize)
                    .offset((long) ((pageNum - 1) * pageSize));

            // 空过滤表达式表示不过滤；分页由 offset/limit 控制
            builder.filter("");

            QueryResp resp = milvusClient.query(builder.build());
            List<QueryResp.QueryResult> results = resp.getQueryResults();
            List<com.lvmaoya.blog.service.rag.RagVectorSearchService.SearchHit> items = new ArrayList<>();
            if (results != null) {
                for (QueryResp.QueryResult r : results) {
                    Map<String, Object> entity = r.getEntity();
                    com.lvmaoya.blog.service.rag.RagVectorSearchService.SearchHit h =
                            new com.lvmaoya.blog.service.rag.RagVectorSearchService.SearchHit();
                    if (entity != null) {
                        Object bid = entity.get("blog_id");
                        Object cidx = entity.get("chunk_index");
                        Object ttl = entity.get("title");
                        Object prev = entity.get("content_preview");
                        h.blogId = bid == null ? null : Long.valueOf(bid.toString());
                        h.chunkIndex = cidx == null ? null : Integer.valueOf(cidx.toString());
                        h.title = ttl == null ? "" : ttl.toString();
                        h.contentPreview = prev == null ? "" : prev.toString();
                        h.score = null; // 非语义搜索，无相似度分数
                    }
                    items.add(h);
                }
            }

            // 统计总量：通过查询迭代器遍历计数（管理端用途，可能较慢）
            int total = -1;
            try {
                QueryIterator iterator = milvusClient.queryIterator(QueryIteratorReq.builder()
                        .collectionName(coll)
                        .expr("")
                        .batchSize(1000L)
                        .outputFields(Collections.singletonList("id"))
                        .build());
                int cnt = 0;
                while (true) {
                    List<QueryResultsWrapper.RowRecord> batch = iterator.next();
                    if (batch == null || batch.isEmpty()) {
                        iterator.close();
                        break;
                    }
                    cnt += batch.size();
                }
                total = cnt;
            } catch (Exception ignored) {
                // 如果统计失败，返回 -1 表示不可用
            }

            payload.put("total", total);
            payload.put("items", items);
            return R.success(payload);
        } catch (Exception e) {
            return R.error(2004, "分页查询失败: " + e.getMessage());
        }
    }
}
