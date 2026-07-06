package com.nextify.blog.common.validator;

import com.nextify.blog.common.ValidationResult;
import org.springframework.web.multipart.MultipartFile;
import java.util.HexFormat;
import java.util.Arrays;

public interface FileValidator {
    ValidationResult validate(MultipartFile file);

    default String bytesToHex(byte[] bytes, int length) {
        if (bytes == null || bytes.length == 0) return "";
        // 使用 Java 17 的原生 HexFormat，性能最高且代码最优雅
        int end = Math.min(length, bytes.length);
        return HexFormat.of().withUpperCase().formatHex(bytes, 0, end);
    }

    String getSupportedType();
}
