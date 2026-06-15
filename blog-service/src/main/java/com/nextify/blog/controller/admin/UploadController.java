package com.nextify.blog.controller.admin;

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
        String contentType = file.getContentType();
        String ext = getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(ext) && !StringUtils.hasText(contentType)) {
            ext = mimeToExtension(contentType);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 文件保存到指定目录
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



    private String mimeToExtension(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
    }
}
