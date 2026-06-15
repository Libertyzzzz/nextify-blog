package com.nextify.blog.common.validator;

import com.nextify.blog.common.ValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class VideoFileValidator implements FileValidator {
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
        "video/mp4", "video/webm", "video/ogg", "video/quicktime"
    );

    private static final String[] ALLOWED_MAGIC_NUMBERS = {
        "0000001866747970", // MP4 (ftyp)
        "52494646",          // WEBM (RIFF)
        "4F676753"           // OGG
    };

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    @Override
    public ValidationResult validate(MultipartFile file) {
        // 1. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.failure("视频大小超出限制（最大50MB）");
        }

        // 2. 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return ValidationResult.failure("视频仅支持 mp4/webm/ogg/quicktime 格式");
        }

        // 3. 文件魔数校验
        try {
            byte[] fileBytes = file.getBytes();
            String magicNumber = bytesToHex(fileBytes, 8);
            boolean isValidMagicNumber = false;
            for (String allowedMagic : ALLOWED_MAGIC_NUMBERS) {
                if (magicNumber.toUpperCase().startsWith(allowedMagic)) {
                    isValidMagicNumber = true;
                    break;
                }
            }
            if (!isValidMagicNumber) {
                return ValidationResult.failure("视频类型验证失败，请上传真实的视频文件");
            }
        } catch (IOException e) {
            return ValidationResult.failure("视频读取失败");
        }

        return ValidationResult.success();
    }

    @Override

    public String getSupportedType() {
        return "video";
    }
}
