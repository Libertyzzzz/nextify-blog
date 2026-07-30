package com.nextify.blog.common.context;

/**
 * 当前登录用户信息上下文
 */
public class UserContextHolder {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除上下文，防止内存泄漏
     * 必须在请求结束后的 finally 块调用
     */
    public static void remove() {
        USER_ID_HOLDER.remove();
    }
}