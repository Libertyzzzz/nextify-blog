package com.nextify.blog.common;


import lombok.Data;

@Data
public class ValidationResult {
    private  boolean valid;
    private  String message;

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, "校验成功");
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(false, "校验失败：" + message);
    }


}
