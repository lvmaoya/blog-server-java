package com.lvmaoya.blog.service.rag;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG向量搜索服务
 * 负责基于语义相似度搜索博客内容
 */
@Service
public class RagVectorSearchService {
    private static final Logger log = LoggerFactory.getLogger(RagVectorSearchService.class);

    /**
     * 搜索结果项
     */
    public static class SearchHit {
        public Long blogId;
        public Integer chunkIndex;
        public String title;
        public String contentPreview;
        public Float score;

        @Override
        public String toString() {
            return String.format("SearchHit{blogId=%d, score=%.4f, title='%s'}", 
                    blogId, score, title);
        }
    }

    @Resource
    private MilvusClientV2 milvusClient;
    @Resource
    private EmbeddingModel embeddingModel;
    @Resource
    private org.springframework.core.env.Environment env;

    /**
     * 获取集合名称
     */
    private String collectionName() {
        return Optional.ofNullable(env.getProperty("rag.collection.name")).orElse("blog_chunks");
    }

    /**
     * 获取搜索结果数量
     */
    private int topK() {
        String v = env.getProperty("rag.top-k", "8");
        return Integer.parseInt(v);
    }

    /**
     * 基于语义搜索博客内容
     * 
     * @param query 查询文本
     * @param categoryId 可选的分类ID过滤
     * @param publishTimeStart 可选的发布时间起始过滤
     * @param publishTimeEnd 可选的发布时间结束过滤
     * @return 搜索结果列表
     */
    public List<SearchHit> searchBySemantic(String query, Long categoryId, Long publishTimeStart, Long publishTimeEnd) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Empty query provided for semantic search");
            return Collections.emptyList();
        }
        
        long startTime = System.currentTimeMillis();
        log.info("Starting semantic search for query: '{}', categoryId: {}, timeRange: [{} to {}]", 
                query, categoryId, publishTimeStart, publishTimeEnd);
        
        String coll = collectionName();
        
        // 确保集合已加载到内存（v2 API）
        try {
            milvusClient.loadCollection(LoadCollectionReq.builder()
                    .collectionName(coll)
                    .build());
        } catch (Exception e) {
            log.error("Error loading collection: ", e);
            return Collections.emptyList();
        }

        // 生成查询向量
        float[] arr;
        try {
            arr = embeddingModel.embed(query);
        } catch (Exception e) {
            log.error("Error generating embedding for query: ", e);
            return Collections.emptyList();
        }
        
        // v2 搜索以 FloatVec 传入

        // 设置搜索参数
        Map<String, Object> params = new HashMap<>();
        params.put("metric_type", "COSINE");

        // 构建过滤条件
        StringBuilder filter = new StringBuilder();
        boolean hasPrev = false;
        
        if (categoryId != null) {
            filter.append("category_id == ").append(categoryId);
            hasPrev = true;
        }
        
        if (publishTimeStart != null) {
            if (hasPrev) filter.append(" and ");
            filter.append("publish_time >= ").append(publishTimeStart);
            hasPrev = true;
        }
        
        if (publishTimeEnd != null) {
            if (hasPrev) filter.append(" and ");
            filter.append("publish_time <= ").append(publishTimeEnd);
        }

        // 构建搜索请求
        SearchReq.SearchReqBuilder builder = SearchReq.builder()
                .collectionName(coll)
                .annsField("embedding")
                .data(Collections.singletonList(new FloatVec(arr)))
                .topK(topK())
                .outputFields(Arrays.asList("blog_id", "chunk_index", "title", "content_preview"))
                .searchParams(params);

        if (filter.length() > 0) {
            builder.filter(filter.toString());
        }

        // 执行搜索并解析结果（v2 API）
        List<SearchHit> hits;
        try {
            SearchResp searchResp = milvusClient.search(builder.build());
            List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
            if (searchResults == null || searchResults.isEmpty()) {
                return Collections.emptyList();
            }

            List<SearchResp.SearchResult> results = searchResults.get(0);
            hits = new ArrayList<>(results.size());
            for (SearchResp.SearchResult r : results) {
                SearchHit h = new SearchHit();
                h.score = r.getScore();
                Map<String, Object> entity = r.getEntity();
                if (entity != null) {
                    Object bid = entity.get("blog_id");
                    Object cidx = entity.get("chunk_index");
                    Object ttl = entity.get("title");
                    Object prev = entity.get("content_preview");
                    h.blogId = bid == null ? null : Long.valueOf(bid.toString());
                    h.chunkIndex = cidx == null ? null : Integer.valueOf(cidx.toString());
                    h.title = ttl == null ? "" : ttl.toString();
                    h.contentPreview = prev == null ? "" : prev.toString();
                }
                hits.add(h);
            }
        } catch (Exception e) {
            log.error("Error executing search: ", e);
            return Collections.emptyList();
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Semantic search completed in {}ms, found {} results", duration, hits.size());
        
        // 记录搜索结果的前几项
        if (!hits.isEmpty()) {
            int logLimit = Math.min(3, hits.size());
            for (int i = 0; i < logLimit; i++) {
                log.debug("Result {}: {}", i+1, hits.get(i));
            }
        }
        
        return hits;
    }
    
    /**
     * 基于语义搜索博客内容（简化版本，无过滤条件）
     * 
     * @param query 查询文本
     * @return 搜索结果列表
     */
    public List<SearchHit> searchBySemantic(String query) {
        return searchBySemantic(query, null, null, null);
    }
}