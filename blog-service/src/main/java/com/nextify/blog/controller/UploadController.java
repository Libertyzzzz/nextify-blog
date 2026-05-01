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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    @Value("${nextify.upload.local-path:uploads/images}")
    private String uploadLocalPath;

    @Value("${nextify.upload.max-image-size:5242880}")
    private long maxImageSize;

    @Value("${nextify.upload.public-base-url:http://localhost:8080/api}")
    private String publicBaseUrl;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail("图片不能为空");
        }
        if (file.getSize() > maxImageSize) {
            return Result.fail("图片大小超出限制");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            return Result.fail("仅支持 jpg/jpeg/png/webp/gif");
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(ext)) {
            ext = mimeToExtension(contentType);
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path uploadDir = Paths.get(uploadLocalPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

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
