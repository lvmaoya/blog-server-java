package com.lvmaoya.blog.blog.service;

import com.lvmaoya.blog.utils.RedisCacheUtil;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class OnlineService {

    private static final String ONLINE_SET_KEY = "online:clients";
    private static final String CLIENT_PREFIX = "online:client:";
    private static final long EXPIRE_MINUTES = 1;
    private static final long EXPIRE_SECONDS = EXPIRE_MINUTES * 60;

    private final RedisCacheUtil redisCacheUtil;

    public OnlineService(RedisCacheUtil redisCacheUtil) {
        this.redisCacheUtil = redisCacheUtil;
    }

    public void heartbeat(String clientId) {
        // 1. 记录/更新客户端最后活跃时间
        redisCacheUtil.set(
                CLIENT_PREFIX + clientId,
                System.currentTimeMillis(),
                EXPIRE_SECONDS
        );

        // 2. 添加到在线集合（Set类型）
        redisCacheUtil.addToSet(ONLINE_SET_KEY, clientId);
    }

    public long getOnlineCount() {
        cleanExpiredClients();
        Long count = redisCacheUtil.getSetSize(ONLINE_SET_KEY);
        return count != null ? count : 0;
    }

    private void cleanExpiredClients() {
        Set<Object> clientIds = redisCacheUtil.getSetMembers(ONLINE_SET_KEY);
        if (clientIds == null || clientIds.isEmpty()) {
            return;
        }

        long cutoff = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(EXPIRE_MINUTES);

        clientIds.forEach(clientId -> {
            String clientKey = CLIENT_PREFIX + clientId;
            Long lastActive = (Long) redisCacheUtil.get(clientKey);

            if (lastActive == null || lastActive < cutoff) {
                // 从Set中移除过期客户端
                redisCacheUtil.removeFromSet(ONLINE_SET_KEY, clientId);
                // 删除客户端时间记录
                redisCacheUtil.delete(clientKey);
            }
        });
    }
}