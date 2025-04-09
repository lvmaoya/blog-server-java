package com.lvmaoya.blog.utils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 设置指定key的value，并可以指定过期时间（单位：秒）
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value);
        if (timeout > 0) {
            expire(key, timeout, TimeUnit.SECONDS);
        }
    }

    // 设置指定key的value，默认不过期（永久保存，除非手动删除或者内存满等情况触发Redis的淘汰策略）
    public void set(String key, Object value) {
        set(key, value, -1);
    }

    // 根据key获取对应的value
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 判断指定key是否存在
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 删除指定的key
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 设置key的过期时间（单位：指定的时间单位）
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    // 获取key剩余的过期时间（单位：秒），返回 -1表示永久有效，-2表示不存在
    public long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    // Set集合操作扩展
    public void addToSet(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public void removeFromSet(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // 批量删除
    public void deleteKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}