package com.nextify.blog.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 遵循 Apple 风格的极简与规范
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private Integer code;    // 状态码
    private String message;  // 提示信息
    private T data;          // 数据负载
    private Long timestamp;  // 响应时间戳

    /**
     * 成功返回 - 无数据
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回 - 有数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(),
                data,
                System.currentTimeMillis()
        );
    }

    /**
     * 失败返回 - 使用默认错误码
     */
    public static <T> Result<T> fail() {
        return fail(ResultCode.ERROR.getMessage());
    }

    /**
     * 失败返回 - 自定义错误信息
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(
                ResultCode.ERROR.getCode(),
                message,
                null,
                System.currentTimeMillis()
        );
    }

    /**
     * 失败返回 - 指定状态码枚举
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(
                resultCode.getCode(),
                resultCode.getMessage(),
                null,
                System.currentTimeMillis()
        );
    }

    public static <T> Result<T> fail(int resultCode, String msg){
        return new Result<T>(
                resultCode,
                msg,
                null,
                System.currentTimeMillis()
        );
    }

}