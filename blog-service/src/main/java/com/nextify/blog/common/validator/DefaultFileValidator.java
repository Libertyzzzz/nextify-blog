package com.nextify.blog.common.validator;

import com.nextify.blog.common.ValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DefaultFileValidator implements FileValidator {
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    @Override
    public ValidationResult validate(MultipartFile file) {
        // 只做大小限制
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.failure("文件大小超出限制（最大100MB）");
        }
        return ValidationResult.success();
    }

    @Override
    public String getSupportedType() {
        return "default";
    }
}
