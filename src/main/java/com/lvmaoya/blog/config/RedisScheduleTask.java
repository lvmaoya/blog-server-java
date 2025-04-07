package com.lvmaoya.blog.config;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@EnableScheduling
public class RedisScheduleTask {

    public static final Log log = LogFactory.getLog(RedisScheduleTask.class);

    @Resource
    private StringRedisTemplate dupShowMasterRedisTemplate;

    // 1 minutes
    @Scheduled(fixedRate = 60000)
    private void configureTasks() {
        log.debug("ping redis");
        dupShowMasterRedisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(@NotNull RedisConnection connection) throws DataAccessException {
                return connection.ping();
            }
        });
    }

}