package com.nextify.blog.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IPUtils {

    public static String getRealIp(HttpServletRequest request) {
        // Cloudflare 专属请求头，最准确
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多重代理的情况，第一个IP才是真实的
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public static boolean isIPv6(String ip) {
        return ip != null && ip.contains(":");
    }

    public static boolean isIPv4(String ip) {
        return !isIPv6(ip);
    }
}
