package com.lvmaoya.blog.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {

    @Value("${rag.milvus.host:localhost}")
    private String host;

    @Value("${rag.milvus.port:19530}")
    private Integer port;

    @Bean
    public MilvusClientV2 milvusClient() {
        ConnectConfig config = ConnectConfig.builder()
                .uri("http://" + host + ":" + port)
                .build();
        return new MilvusClientV2(config);
    }
}