package com.nextify.blog.common.context;

import com.nextify.blog.dto.UserPermissionInfo;

public class UserContextHolder {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<UserPermissionInfo> PERMISSION_INFO_HOLDER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setPermissionInfo(UserPermissionInfo info) {
        PERMISSION_INFO_HOLDER.set(info);
    }

    public static UserPermissionInfo getPermissionInfo() {
        return PERMISSION_INFO_HOLDER.get();
    }

    public static void remove() {
        USER_ID_HOLDER.remove();
        PERMISSION_INFO_HOLDER.remove();
    }
}