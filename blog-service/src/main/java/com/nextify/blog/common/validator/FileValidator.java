package com.nextify.blog.common.validator;

import com.nextify.blog.common.ValidationResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileValidator {
    ValidationResult validate(MultipartFile file);
    default String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(length, bytes.length); i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }

    String getSupportedType();
}
