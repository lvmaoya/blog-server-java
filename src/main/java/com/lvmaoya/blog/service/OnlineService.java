package com.lvmaoya.blog.service;

public interface OnlineService {
    /**
     * 处理客户端心跳
     * @param clientId 客户端唯一ID
     */
    void heartbeat(String clientId);

    /**
     * 获取当前在线人数
     * @return 在线人数
     */
    long getOnlineCount();
}