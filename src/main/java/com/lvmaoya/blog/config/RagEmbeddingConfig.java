package com.lvmaoya.blog.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagEmbeddingConfig {

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public EmbeddingModel embeddingModel() {
        // 复用 Spring AI OpenAI 兼容接口
        OpenAiApi api = new OpenAiApi(baseUrl, apiKey);
        return new OpenAiEmbeddingModel(api);
    }
}