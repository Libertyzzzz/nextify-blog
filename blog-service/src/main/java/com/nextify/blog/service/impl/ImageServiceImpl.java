package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.entity.BlogImage;
import com.nextify.blog.mapper.BlogImageMapper;
import com.nextify.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ImageServiceImpl extends ServiceImpl<BlogImageMapper, BlogImage> implements ImageService {
    @Value("${nextify.upload.local-path:uploads/images}")
    private String uploadLocalPath;


    @Value("${nextify.upload.public-base-url:http://localhost:8080/api}")
    private String publicBaseUrl;


    @Override
    public void deleteImage(String imageId) {
        this.removeById(imageId);
    }

    @Override
    public void forceDeleteImage(String imageId) {
        this.removeById(imageId);
    }

    @Override
    public List<BlogImage> listImages() {
        return this.list();
    }

    @Override
    public Map<String, String> uploadImageWithReference(MultipartFile file, String usageType, Long usageId)  {
        if(file == null)
            return null;
        String ext = getExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(ext)) {
            ext = mimeToExtension(Objects.requireNonNull(file.getContentType()));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 7. 文件保存到指定目录
        try {
            Path uploadDir = Paths.get(uploadLocalPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            // 安全检查
            Path targetPath = uploadDir.resolve(fileName).normalize();
            if (!targetPath.startsWith(uploadDir)) {
                // 抛出更具体的异常
                throw new RuntimeException("文件路径不安全，可能存在目录遍历攻击");
            }
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // 改进异常消息
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }

        String relativeUrl = "/uploads/" + fileName;
        String fullUrl = publicBaseUrl + relativeUrl;

        BlogImage curr = new BlogImage();
        curr.setFileName(fileName);
        curr.setOriginalName(file.getOriginalFilename());
        curr.setSize(file.getSize());
        curr.setMimeType(file.getContentType());
        curr.setUsageType(usageType);
        curr.setUsageId(usageId);
        curr.setPath(relativeUrl);
        curr.setUrl(fullUrl);
        curr.setIsTemporary(usageId == null); // 根据usageId是否为空设置isTemporary
        this.save(curr);

        Map<String, String> data = new HashMap<>();
        data.put("url", fullUrl);
        data.put("path", relativeUrl);
        data.put("name", fileName);
        data.put("id", curr.getId() != null ? curr.getId().toString() : null); // 返回生成的图片ID
        return data;
    }

    @Override
    public void updateImageUsage(List<Long> imageIds, Long articleId) {
        if (imageIds != null && !imageIds.isEmpty()) {
            LambdaUpdateWrapper<BlogImage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(BlogImage::getId, imageIds)
                         .set(BlogImage::getUsageId, articleId)
                         .set(BlogImage::getIsTemporary, false);


            this.update(updateWrapper);
        }
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