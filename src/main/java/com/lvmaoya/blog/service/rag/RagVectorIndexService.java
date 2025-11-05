package com.lvmaoya.blog.service.rag;

import com.lvmaoya.blog.domain.entity.Blog;
import com.lvmaoya.blog.domain.entity.BlogContent;
import com.lvmaoya.blog.mapper.BlogContentMapper;
import com.lvmaoya.blog.mapper.BlogMapper;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq.CollectionSchema;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DropIndexReq;
import io.milvus.v2.common.IndexParam;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import com.lvmaoya.blog.service.rag.ZhipuEmbeddingService;
import java.lang.reflect.Field;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * RAG向量索引服务
 * 负责创建、重建和更新Milvus向量索引
 */
@Service
public class RagVectorIndexService {
    private static final Logger log = LoggerFactory.getLogger(RagVectorIndexService.class);
    private static final int BATCH_SIZE = 1000; // 批量插入的大小

    @Resource
    private MilvusClientV2 milvusClient;
    @Resource
    private ZhipuEmbeddingService zhipuEmbeddingService;
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private BlogContentMapper blogContentMapper;
    @Resource
    private org.springframework.core.env.Environment env;

    private String collectionName() {
        return Optional.ofNullable(env.getProperty("rag.collection.name")).orElse("blog_chunks");
    }

    private int embeddingDim() {
        String val = env.getProperty("rag.embedding.dim", "1536");
        return Integer.parseInt(val);
    }

    /**
     * 创建或加载集合
     * 如果集合不存在，则创建并设置索引
     */
    public void createOrLoadCollection() {
        String coll = collectionName();
        Boolean exists = milvusClient.hasCollection(HasCollectionReq.builder()
                .collectionName(coll)
                .build());

        if (!Boolean.TRUE.equals(exists)) {
            log.info("Creating collection: {}", coll);
            try {
                // 定义集合字段（v2）
                List<AddFieldReq> fields = new ArrayList<>();
                fields.add(AddFieldReq.builder()
                        .fieldName("id")
                        .dataType(DataType.Int64)
                        .isPrimaryKey(true)
                        .autoID(true)
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("embedding")
                        .dataType(DataType.FloatVector)
                        .dimension(embeddingDim())
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("blog_id")
                        .dataType(DataType.Int64)
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("chunk_index")
                        .dataType(DataType.Int32)
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("category_id")
                        .dataType(DataType.Int64)
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("publish_time")
                        .dataType(DataType.Int64)
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("title")
                        .dataType(DataType.VarChar)
                        .maxLength(512)
                        .build());

                fields.add(AddFieldReq.builder()
                        .fieldName("content_preview")
                        .dataType(DataType.VarChar)
                        .maxLength(2048)
                        .build());

                CollectionSchema schema = CollectionSchema.builder()
                        .enableDynamicField(false)
                        .build();
                for (AddFieldReq f : fields) {
                    schema.addField(f);
                }

                milvusClient.createCollection(CreateCollectionReq.builder()
                        .collectionName(coll)
                        .description("Blog chunks semantic index")
                        .numShards(2)
                        .collectionSchema(schema)
                        .build());

                // 创建索引 - 使用 IndexParam + CreateIndexReq.indexParams()
                Map<String, Object> extra = new HashMap<>();
                extra.put("M", 16);
                extra.put("efConstruction", 200);

                IndexParam indexParam = IndexParam.builder()
                        .fieldName("embedding")
                        .indexName("embedding_index")
                        .indexType(IndexParam.IndexType.HNSW)
                        .metricType(IndexParam.MetricType.COSINE)
                        .extraParams(extra)
                        .build();

                List<IndexParam> indexParams = new ArrayList<>();
                indexParams.add(indexParam);

                milvusClient.createIndex(CreateIndexReq.builder()
                        .collectionName(coll)
                        .indexParams(indexParams)
                        .build());
            } catch (Exception e) {
                log.error("Failed to create collection or index: ", e);
                return;
            }
        }
        // 加载集合到内存（v2）
        try {
            milvusClient.loadCollection(LoadCollectionReq.builder()
                    .collectionName(coll)
                    .build());
        } catch (Exception e) {
            log.error("Failed to load collection: ", e);
        }
    }

    /**
     * 重建向量索引
     * 删除现有集合并重新创建，然后索引所有博客内容
     */
    public void rebuildVectorIndex() {
        rebuildVectorIndex(null);
    }

    /**
     * 重建向量索引，支持限制处理的博客数量用于测试
     * @param limit 可选，限制处理的前 N 篇文章
     */
    public void rebuildVectorIndex(Integer limit) {

        long startTime = System.currentTimeMillis();
        log.info("Starting vector index rebuild...");

        String coll = collectionName();
        // 若集合已存在，删除后重建
        Boolean has = milvusClient.hasCollection(HasCollectionReq.builder()
                .collectionName(coll)
                .build());

        if (Boolean.TRUE.equals(has)) {
            try {
                // 先释放集合，避免“集合已加载”导致无法删除索引
                milvusClient.releaseCollection(ReleaseCollectionReq.builder()
                        .collectionName(coll)
                        .build());
            } catch (Exception e) {
                log.warn("Release collection failed (may not be loaded): {}", e.getMessage());
            }

            try {
                milvusClient.dropIndex(DropIndexReq.builder()
                        .collectionName(coll)
                        .indexName("embedding_index")
                        .build());
            } catch (Exception e) {
                log.warn("Drop index failed (may not exist): {}", e.getMessage());
            }

            try {
                milvusClient.dropCollection(DropCollectionReq.builder()
                        .collectionName(coll)
                        .build());
            } catch (Exception e) {
                log.warn("Drop collection failed: {}", e.getMessage());
            }
        }

        createOrLoadCollection();

        // 获取所有博客及其内容
        List<Blog> blogs = blogMapper.selectList(null);
        if (limit != null && limit > 0 && blogs.size() > limit) {
            blogs = blogs.subList(0, Math.min(limit, blogs.size()));
            log.info("Limiting rebuild to first {} blogs for testing", blogs.size());
        }
        log.info("Found {} blogs to index", blogs.size());

        Map<Long, BlogContent> contents = new HashMap<>();
        for (Blog b : blogs) {
            BlogContent bc = blogContentMapper.selectById(b.getId());
            if (bc != null) contents.put(Long.valueOf(b.getId()), bc);
        }

        int totalChunks = 0;
        int processedBlogs = 0;

        // 批量处理博客，避免一次性处理过多数据
        List<List<Float>> embeddings = new ArrayList<>();
        List<Long> blogIds = new ArrayList<>();
        List<Integer> chunkIdx = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        List<Long> publishTimes = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<String> previews = new ArrayList<>();

        for (Blog blog : blogs) {
            Integer bid = safeField(blog, "id", Integer.class);
            if (bid == null) continue;

            BlogContent bc = contents.get(bid.longValue());
            String content = bc != null ? safeField(bc, "content", String.class) : null;
            if (content == null) continue;

            List<String> chunks = chunkText(content, 500);

            // 批量生成嵌入，显著减少嵌入 API 的调用次数
            List<List<Float>> chunkEmbeddings = embedBatch(chunks);
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                List<Float> fvec;
                if (chunkEmbeddings != null && i < chunkEmbeddings.size()) {
                    fvec = chunkEmbeddings.get(i);
                } else {
                    // 回退：若批量失败或数量不匹配，单条生成（智谱）
                    float[] arr = zhipuEmbeddingService.embed(chunk);
                    fvec = new ArrayList<>(arr.length);
                    for (float v : arr) fvec.add(v);
                }

                embeddings.add(fvec);
                blogIds.add(bid.longValue());
                chunkIdx.add(i);

                Integer catId = safeField(blog, "categoryId", Integer.class);
                categoryIds.add(catId == null ? 0L : catId.longValue());

                java.util.Date pub = safeField(blog, "publishedTime", java.util.Date.class);
                long pt = (pub == null) ? 0L : pub.getTime();
                publishTimes.add(pt);

                String title = safeField(blog, "title", String.class);
                titles.add(safePreview(title, 512));
                previews.add(safePreview(chunk, 2048));

                totalChunks++;

                // 批量插入，避免一次性插入过多数据
                if (embeddings.size() >= BATCH_SIZE) {
                    insertBatch(coll, embeddings, blogIds, chunkIdx, categoryIds, publishTimes, titles, previews);

                    // 清空批次数据
                    embeddings.clear();
                    blogIds.clear();
                    chunkIdx.clear();
                    categoryIds.clear();
                    publishTimes.clear();
                    titles.clear();
                    previews.clear();
                }
            }

            processedBlogs++;
            if (processedBlogs % 100 == 0) {
                log.info("Processed {}/{} blogs, {} chunks so far", processedBlogs, blogs.size(), totalChunks);
            }
        }

        // 处理剩余的数据
        if (!embeddings.isEmpty()) {
            insertBatch(coll, embeddings, blogIds, chunkIdx, categoryIds, publishTimes, titles, previews);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Vector index rebuild completed in {}s. Indexed {} blogs with {} chunks total",
                TimeUnit.MILLISECONDS.toSeconds(duration), blogs.size(), totalChunks);
    }

    /**
     * 更新或插入单个博客的向量索引
     * @param blogId 博客ID
     */
    public void upsertBlog(Long blogId) {
        if (blogId == null) {
            log.warn("Attempted to upsert blog with null ID");
            return;
        }

        log.info("Upserting blog ID: {}", blogId);
        createOrLoadCollection();
        String coll = collectionName();

        // 删除现有的博客向量（v2）
        try {
            milvusClient.delete(DeleteReq.builder()
                    .collectionName(coll)
                    .filter("blog_id == " + blogId)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to delete existing vectors for blog {}: {}", blogId, e.getMessage());
        }

        // 获取博客及其内容
        Blog blog = blogMapper.selectById(blogId.intValue());
        if (blog == null) {
            log.warn("Blog not found with ID: {}", blogId);
            return;
        }

        BlogContent bc = blogContentMapper.selectById(blogId.intValue());
        if (bc == null) {
            log.warn("Blog content not found for blog ID: {}", blogId);
            return;
        }

        String content = safeField(bc, "content", String.class);
        if (content == null) {
            log.warn("Blog content is null for blog ID: {}", blogId);
            return;
        }

        // 分块并生成向量
        List<String> chunks = chunkText(content, 500);
        log.info("Blog {} split into {} chunks", blogId, chunks.size());

        List<List<Float>> embeddings = new ArrayList<>();
        List<Long> blogIds = new ArrayList<>();
        List<Integer> chunkIdx = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        List<Long> publishTimes = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<String> previews = new ArrayList<>();

        // 批量生成嵌入
        List<List<Float>> chunkEmbeddings = embedBatch(chunks);
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            List<Float> fvec;
            if (chunkEmbeddings != null && i < chunkEmbeddings.size()) {
                fvec = chunkEmbeddings.get(i);
            } else {
                float[] arr = zhipuEmbeddingService.embed(chunk);
                fvec = new ArrayList<>(arr.length);
                for (float v : arr) fvec.add(v);
            }

            embeddings.add(fvec);

            Integer bid = safeField(blog, "id", Integer.class);
            blogIds.add(bid == null ? 0L : bid.longValue());
            chunkIdx.add(i);

            Integer catId = safeField(blog, "categoryId", Integer.class);
            categoryIds.add(catId == null ? 0L : catId.longValue());

            java.util.Date pub = safeField(blog, "publishedTime", java.util.Date.class);
            long pt = (pub == null) ? 0L : pub.getTime();
            publishTimes.add(pt);

            String title = safeField(blog, "title", String.class);
            titles.add(safePreview(title, 512));
            previews.add(safePreview(chunk, 2048));
        }

        // 插入向量
        if (!embeddings.isEmpty()) {
            insertBatch(coll, embeddings, blogIds, chunkIdx, categoryIds, publishTimes, titles, previews);
            log.info("Successfully upserted {} chunks for blog ID: {}", chunks.size(), blogId);
        } else {
            log.warn("No chunks generated for blog ID: {}", blogId);
        }
    }

    /**
     * 批量插入向量数据
     */
    private void insertBatch(String collectionName,
                            List<List<Float>> embeddings,
                            List<Long> blogIds,
                            List<Integer> chunkIdx,
                            List<Long> categoryIds,
                            List<Long> publishTimes,
                            List<String> titles,
                            List<String> previews) {
        try {
            List<JsonObject> rows = new ArrayList<>(embeddings.size());
            for (int i = 0; i < embeddings.size(); i++) {
                JsonObject row = new JsonObject();
                // 向量字段作为 JsonArray 填充
                JsonArray vecArr = new JsonArray();
                for (Float f : embeddings.get(i)) {
                    vecArr.add(f);
                }
                row.add("embedding", vecArr);
                row.addProperty("blog_id", blogIds.get(i));
                row.addProperty("chunk_index", chunkIdx.get(i));
                row.addProperty("category_id", categoryIds.get(i));
                row.addProperty("publish_time", publishTimes.get(i));
                row.addProperty("title", titles.get(i));
                row.addProperty("content_preview", previews.get(i));
                rows.add(row);
            }

            milvusClient.insert(InsertReq.builder()
                    .collectionName(collectionName)
                    .data(rows)
                    .build());

            log.debug("Successfully inserted batch of {} vectors", embeddings.size());
        } catch (Exception e) {
            log.error("Error inserting batch: ", e);
        }
    }

    /**
     * 批量生成嵌入，优先使用批量结果；失败时回退到单条生成
     */
    private List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();
        // 智谱 embedding 接口单次最多 64 条，这里进行分批调用以避免 400 错误
        int limit = env.getProperty("zhipu.embedding.batch.limit", Integer.class, 64);
        List<List<Float>> all = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i += limit) {
            List<String> sub = texts.subList(i, Math.min(texts.size(), i + limit));
            List<List<Float>> part = zhipuEmbeddingService.embedBatch(sub);
            if (part != null && !part.isEmpty()) {
                all.addAll(part);
            } else {
                // 如果批量失败，逐条回退生成，确保尽可能多产出
                for (String s : sub) {
                    float[] arr = zhipuEmbeddingService.embed(s);
                    List<Float> fv = new ArrayList<>(arr.length);
                    for (float v : arr) fv.add(v);
                    all.add(fv);
                }
            }
        }
        return all;
    }

    

    /**
     * 安全截取字符串预览
     */
    private String safePreview(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * 安全获取对象字段值
     * 使用反射获取字段值，避免编译错误
     */
    private <T> T safeField(Object obj, String fieldName, Class<T> type) {
        try {
            if (obj == null) return null;
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object v = f.get(obj);
            return type.cast(v);
        } catch (Exception e) {
            log.debug("Failed to access field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * 将文本分块
     * 按句子分割，并确保每个块不超过目标长度
     */
    private List<String> chunkText(String content, int targetLen) {
        if (content == null || content.isEmpty()) return Collections.emptyList();

        // 分割成句子
        List<String> sentences = new ArrayList<>();
        String tmp = content.replace("\r", "\n");
        String[] lines = tmp.split("\n");

        for (String line : lines) {
            // 按中文句号、感叹号、问号分割
            String[] parts = line.split("(?<=[。！？])");
            sentences.addAll(Arrays.stream(parts)
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList()));
        }

        // 组合成块
        List<String> chunks = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        for (String sen : sentences) {
            // 如果当前块加上新句子会超过目标长度，先保存当前块
            if (cur.length() + sen.length() + 1 > targetLen) {
                if (cur.length() > 0) {
                    chunks.add(cur.toString());
                    cur.setLength(0);
                }
            }

            // 如果单个句子超过目标长度，直接分割
            if (sen.length() > targetLen) {
                for (int i = 0; i < sen.length(); i += targetLen) {
                    chunks.add(sen.substring(i, Math.min(i + targetLen, sen.length())));
                }
            } else {
                // 否则添加到当前块
                if (cur.length() > 0) cur.append('\n');
                cur.append(sen);
            }
        }

        // 添加最后一个块
        if (cur.length() > 0) chunks.add(cur.toString());

        return chunks;
    }
}