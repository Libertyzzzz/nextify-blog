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
import java.io.InputStream;

@Component
public class ImageFileValidator implements FileValidator {
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif", "image/svg+xml"
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

        // 3. SVG 矢量图专项安全校验（XML 结构 + 防 XSS 脚本注入）
        if ("image/svg+xml".equalsIgnoreCase(contentType)) {
            return validateSvg(file);
        }

        String magicNumber = getFileMagicNumber(file);
        boolean isValidMagicNumber =  false;
        for (String allowedMagic : ALLOWED_MAGIC_NUMBERS) {
            if (magicNumber != null && magicNumber.startsWith(allowedMagic)) {
                isValidMagicNumber = true;
                break;
            }
        }
        if (!isValidMagicNumber) {
            return ValidationResult.failure(UploadErrorEnum.INVALID_PARAMETER.getMessage());
        }


        // 4. 使用ImageIO验证图片有效性（防止恶意文件）
        try {
            InputStream is = file.getInputStream();
            BufferedImage image = ImageIO.read(is);
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


    /**
     * 获取文件魔数（十六进制字符串）
     */
    private String getFileMagicNumber(MultipartFile file) {
        try (
            InputStream is = file.getInputStream()) {
            byte[] head = new byte[4];
            if (is.read(head) != -1) {
                return bytesToHex(head);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    /**
     * 字节数组转十六进制
     */

    private String bytesToHex(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) return null;
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) stringBuilder.append(0);
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }




    private ValidationResult validateSvg(MultipartFile file) {
        try {
            String raw = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String lower = raw.toLowerCase(java.util.Locale.ROOT);
            if (!lower.contains("<svg")) {
                return ValidationResult.failure(UploadErrorEnum.INVALID_PARAMETER.getMessage());
            }
            if (lower.contains("<script")
                    || lower.contains("javascript:")
                    || lower.contains("<foreignobject")
                    || lower.matches("(?s).*\\bon(load|error|click|mouseover|focus|animationstart)\\s*=.*")) {
                return ValidationResult.failure("SVG 包含不安全脚本内容，已拒绝上传");
            }
            return ValidationResult.success();
        } catch (IOException e) {
            return ValidationResult.failure(UploadErrorEnum.VALIDATION_FAILURE.getMessage());
        }
    }

    @Override
    public String getSupportedType() {
        return "image";
    }



}
