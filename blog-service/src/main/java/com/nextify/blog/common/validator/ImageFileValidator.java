package com.nextify.blog.common.validator;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.ValidationResult;
import com.nextify.blog.enums.UploadErrorEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class ImageFileValidator implements FileValidator {
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private static final String[] ALLOWED_MAGIC_NUMBERS = {
        "FFD8FF",    // JPEG
        "89504E47",  // PNG
        "52494646",  // WEBP/RIFF
        "47494638"   // GIF
    };

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    @Override
    public ValidationResult validate(MultipartFile file) {

        // 1. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.failure(UploadErrorEnum.FILE_TOO_LARGE.getMessage());
        }

        // 2. 文件类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return ValidationResult.failure(UploadErrorEnum.INVALID_FILE_TYPE.getMessage());
        }

        byte[] fileBytes = null;
        // 3. 文件魔数校验
        try {
            fileBytes = file.getBytes();
            String magicNumber = bytesToHex(fileBytes, 4);
            boolean isValidMagicNumber = false;
            for (String allowedMagic : ALLOWED_MAGIC_NUMBERS) {
                if (magicNumber.toUpperCase().startsWith(allowedMagic)) {
                    isValidMagicNumber = true;
                    break;
                }
            }
            if (!isValidMagicNumber) {
                return ValidationResult.failure(UploadErrorEnum.INVALID_PARAMETER.getMessage());
            }
        } catch (IOException e) {
            return ValidationResult.failure(UploadErrorEnum.UPLOAD_FAILED.getMessage());
        }

        // 4. 使用ImageIO验证图片有效性（防止恶意文件）
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            if (image == null) {
                return ValidationResult.failure(UploadErrorEnum.INVALID_PARAMETER.getMessage());
            }
            // 验证图片尺寸（防止超大图片DoS攻击）
            if (image.getWidth() > 10000 || image.getHeight() > 10000) {
                return ValidationResult.failure(UploadErrorEnum.FILE_TOO_LARGE.getMessage());
            }
        } catch (IOException e) {
            return ValidationResult.failure(UploadErrorEnum.VALIDATION_FAILURE.getMessage());
        }

        return ValidationResult.success();
    }

    @Override
    public String getSupportedType() {
        return "image";
    }



}
