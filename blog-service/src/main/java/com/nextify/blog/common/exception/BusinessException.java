package com.nextify.blog.common.exception;

import com.nextify.blog.common.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在 Service 层主动抛出，由 GlobalExceptionHandler 统一捕获
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}