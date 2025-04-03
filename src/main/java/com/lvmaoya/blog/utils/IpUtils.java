package com.lvmaoya.blog.utils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * IP地址工具类
 */
@Component
@Slf4j
public class IpUtils {

    /**
     * 获取客户端真实IP地址
     * @param request HttpServletRequest对象
     * @return IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        Objects.requireNonNull(request, "HttpServletRequest不能为null");

        // 1. 尝试从X-Forwarded-For获取
        String ip = request.getHeader("X-Forwarded-For");

        // 2. 如果X-Forwarded-For为空或unknown，尝试其他头
        if (isInvalidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        // 3. 最后使用request.getRemoteAddr()
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 4. 处理多个IP的情况（如X-Forwarded-For: client, proxy1, proxy2）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 5. 本地开发环境处理
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }

    /**
     * 判断IP地址是否无效
     */
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }

    /**
     * 将IPv4地址转换为long类型
     */
    public static long ipToLong(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {
            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);
        }
        return result;
    }

    /**
     * 将long类型转换为IPv4地址
     */
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }
}
