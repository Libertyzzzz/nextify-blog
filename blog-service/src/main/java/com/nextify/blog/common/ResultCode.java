package com.nextify.blog.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 默认失败
    ERROR(500, "系统繁忙，请稍后再试"),

    // 认证相关 (Apple 风格的严谨性)
    UNAUTHORIZED(401, "尚未登录或登录已过期"),
    FORBIDDEN(403, "权限不足，拒绝访问"),

    // 业务相关
    VALIDATE_FAILED(400, "参数检验失败"),
    NOT_FOUND(404, "资源不存在"),
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    TOKEN_INVALID(1003, "Token无效或已过期");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}