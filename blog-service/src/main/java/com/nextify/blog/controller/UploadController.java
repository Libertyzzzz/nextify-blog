package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * 文件上传控制器
 * 安全措施：
 * 1. 限制文件类型（白名单）
 * 2. 验证文件魔数（真实类型检测）
 * 3. 使用ImageIO验证图片有效性
 * 4. 限制文件大小
 * 5. 文件存储在非Web可访问目录
 */
@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    // 文件魔数白名单（十六进制）
    private static final String[] ALLOWED_MAGIC_NUMBERS = {
            "FFD8FF",  // JPEG
            "89504E47", // PNG
            "52494646", // WEBP/RIFF
            "47494638"  // GIF
    };

    @Value("${nextify.upload.local-path:uploads/images}")
    private String uploadLocalPath;

    @Value("${nextify.upload.max-image-size:5242880}")
    private long maxImageSize;

    @Value("${nextify.upload.public-base-url:http://localhost:8080/api}")
    private String publicBaseUrl;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        // 1. 文件非空验证
        if (file == null || file.isEmpty()) {
            return Result.fail("图片不能为空");
        }
        // 2. 文件大小验证
        if (file.getSize() > maxImageSize) {
            return Result.fail("图片大小超出限制");
        }
        // 3. 文件类型验证
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return Result.fail("仅支持 jpg/jpeg/png/webp/gif");
        }

        // 4. 文件魔数验证（防止文件伪装）
        byte[] fileBytes = file.getBytes();
        String magicNumber = bytesToHex(fileBytes, 4);
        boolean isValidMagicNumber = false;
        for (String allowedMagic : ALLOWED_MAGIC_NUMBERS) {
            if (magicNumber.toUpperCase().startsWith(allowedMagic)) {
                isValidMagicNumber = true;
                break;
            }
        }

        if (!isValidMagicNumber) {
            return Result.fail("文件类型验证失败，请上传真实的图片文件");
        }

        // 5. 使用ImageIO验证图片有效性（防止恶意文件）
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            if (image == null) {
                return Result.fail("图片文件损坏或格式无效");
            }
            // 验证图片尺寸（防止超大图片DoS攻击）
            if (image.getWidth() > 10000 || image.getHeight() > 10000) {
                return Result.fail("图片尺寸过大");
            }
        } catch (IOException e) {
            return Result.fail("图片文件验证失败");
        }

        // 6. 文件名生成
        String ext = getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(ext)) {
            ext = mimeToExtension(contentType);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 7. 文件保存到指定目录
        Path uploadDir = Paths.get(uploadLocalPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        // 安全检查
        Path targetPath = uploadDir.resolve(fileName).normalize();
        if (!targetPath.startsWith(uploadDir)) {
            return Result.fail("文件路径不安全");
        }
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String relativeUrl = "/uploads/" + fileName;
        String fullUrl = publicBaseUrl + relativeUrl;

        Map<String, String> data = new HashMap<>();
        data.put("url", fullUrl);
        data.put("path", relativeUrl);
        data.put("name", fileName);
        return Result.success(data);
    }

    private String getExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(length, bytes.length); i++) {
            sb.append(String.format("%02X", bytes[i]));
        }

        List<int[]> res = new ArrayList<>();
        res.toArray(new int[res.size()][]);
        return sb.toString();
    }

    private String mimeToExtension(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
    }
}
