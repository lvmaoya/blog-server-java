package com.lvmaoya.blog.chat.service;

import ai.z.openapi.ZhipuAiClient;

import ai.z.openapi.service.embedding.EmbeddingCreateParams;
import ai.z.openapi.service.embedding.EmbeddingResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 智谱 Embedding 服务（SDK版，使用 embedding-3）
 */
@Service
public class ZhipuEmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(ZhipuEmbeddingService.class);

    @Resource
    private org.springframework.core.env.Environment env;

    private ZhipuAiClient client;

    @PostConstruct
    public void init() {
        String apiKey = env.getProperty("zhipu.api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Zhipu API key is missing. Set 'zhipu.api-key' in application.yml or environment.");
            return;
        }
        client = ZhipuAiClient.builder().apiKey(apiKey).build();
    }

    private String model() {
        // 默认使用 embedding-3
        return Optional.ofNullable(env.getProperty("zhipu.embedding.model")).orElse("embedding-3");
    }

    private Integer dimensions() {
        // 优先与 Milvus 集合维度一致：rag.embedding.dim
        Integer dimFromRag = env.getProperty("rag.embedding.dim", Integer.class);
        if (dimFromRag != null && dimFromRag > 0) return dimFromRag;
        return env.getProperty("zhipu.embedding.dimensions", Integer.class, 768);
    }

    /**
     * 单文本嵌入
     */
    public float[] embed(String text) {
        List<List<Float>> batch = embedBatch(Collections.singletonList(text));
        if (batch == null || batch.isEmpty()) return new float[0];
        List<Float> vec = batch.get(0);
        float[] arr = new float[vec.size()];
        for (int i = 0; i < vec.size(); i++) arr[i] = vec.get(i);
        return arr;
    }

    /**
     * 批量嵌入（embedding-3 支持自定义维度）
     */
    public List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();
        if (client == null) {
            log.error("Zhipu client not initialized. Please configure 'zhipu.api-key'.");
            return Collections.emptyList();
        }
        try {
            EmbeddingCreateParams request = EmbeddingCreateParams.builder()
                    .model(model())
                    .input(texts)
                    .dimensions(dimensions())
                    .build();

            EmbeddingResponse response = client.embeddings().createEmbeddings(request);
            if (response == null) {
                log.warn("Zhipu embedding response is null");
                return Collections.emptyList();
            }

            Object dataObj = response.getData();
            List<?> items = Collections.emptyList();
            if (dataObj instanceof List<?>) {
                items = (List<?>) dataObj;
            } else if (dataObj != null) {
                try {
                    java.lang.reflect.Method m = dataObj.getClass().getMethod("getData");
                    Object inner = m.invoke(dataObj);
                    if (inner instanceof List<?>) {
                        items = (List<?>) inner;
                    }
                } catch (Exception ignore) {
                    log.debug("Zhipu embedding response.getData() is not a List; tried nested getData()");
                }
            }

            if (items == null || items.isEmpty()) {
                log.warn("Zhipu embedding response has no items");
                return Collections.emptyList();
            }

            // 排序保证与输入顺序一致（如果返回包含index）
            try {
                items.sort((a, b) -> Integer.compare(
                        Optional.ofNullable(extractIndex(a)).orElse(0),
                        Optional.ofNullable(extractIndex(b)).orElse(0))
                );
            } catch (Exception e) {
                log.debug("Items sort by index skipped: {}", e.getMessage());
            }

            List<List<Float>> out = new ArrayList<>(items.size());
            for (Object item : items) {
                List<Double> emb = extractEmbedding(item);
                List<Float> fv = new ArrayList<>(emb.size());
                for (Double v : emb) fv.add(v == null ? 0f : v.floatValue());
                out.add(fv);
            }
            return out;
        } catch (Exception e) {
            log.error("Zhipu embedding SDK call failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Integer extractIndex(Object item) {
        try {
            java.lang.reflect.Method m = item.getClass().getMethod("getIndex");
            Object val = m.invoke(item);
            return (val instanceof Integer) ? (Integer) val : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Double> extractEmbedding(Object item) {
        try {
            java.lang.reflect.Method m = item.getClass().getMethod("getEmbedding");
            Object val = m.invoke(item);
            if (val instanceof List<?>) {
                return (List<Double>) val;
            }
        } catch (Exception ignore) { }
        // 备用方法名兼容
        try {
            java.lang.reflect.Method m = item.getClass().getMethod("getVector");
            Object val = m.invoke(item);
            if (val instanceof List<?>) {
                return (List<Double>) val;
            }
        } catch (Exception ignore) { }
        return Collections.emptyList();
    }
}