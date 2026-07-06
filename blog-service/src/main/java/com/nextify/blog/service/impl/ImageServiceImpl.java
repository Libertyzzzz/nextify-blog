package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.ImageDeleteDto;
import com.nextify.blog.dto.ImageQueryDto;
import com.nextify.blog.dto.ImageReferenceDto;
import com.nextify.blog.entity.BlogImage;
import com.nextify.blog.enums.ImageUsageType;
import com.nextify.blog.mapper.BlogImageMapper;
import com.nextify.blog.vo.ImageDeleteResultVo;
import com.nextify.blog.service.BlogArticleService;
import com.nextify.blog.service.ImageService;
import com.nextify.blog.vo.ImageInfoVo;
import com.nextify.blog.vo.ImageReferenceVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageServiceImpl extends ServiceImpl<BlogImageMapper, BlogImage> implements ImageService {
    @Value("${nextify.upload.local-path:uploads/images}")
    private String uploadLocalPath;


    @Value("${nextify.upload.public-base-url:http://localhost:8080/api}")
    private String publicBaseUrl;


    @Lazy // 防止循环依赖
    @Resource
    private BlogArticleService articleService;


    @Override
    public ImageDeleteResultVo deleteImage(ImageDeleteDto request) {
        List<Long> ids = request.getIds();
        if (ids == null || ids.isEmpty()) {
            return ImageDeleteResultVo.builder().successCount(0).failCount(0).build();
        }

        // 1. 批量查询这些图片的状态
        List<BlogImage> images = this.listByIds(ids);
        if(CollectionUtils.isEmpty(images)){
            log.info("Image list query  is empty");
            return ImageDeleteResultVo.builder().successCount(0).failCount(0).build();
        }
        List<Long> deletableIds = new ArrayList<>();
        List<String> errorMsgs = new ArrayList<>();

        for (BlogImage img : images) {
            // 2. 检查引用计数
            if (img.getReferenceCount() != null && img.getReferenceCount() > 0) {
                errorMsgs.add("图片 [" + img.getOriginalName() + "] 仍有 " + img.getReferenceCount() + " 处引用，无法删除");
            } else {
                deletableIds.add(img.getId());
            }
        }

        // 3. 仅执行可删除的
        if (!deletableIds.isEmpty()) {
            this.lambdaUpdate()
                .in(BlogImage::getId, deletableIds)
                .set(BlogImage::getDeleteTime, LocalDateTime.now())
                .update();

            // TODO: 触发异步任务删除磁盘上的物理文件 (Paths.get(uploadLocalPath, img.getFileName()))
            // 这里的逻辑建议参考之前讨论的 TransactionSynchronizationManager
        }

        // 4. 返回删除报告
        return ImageDeleteResultVo.builder()
                .successCount(deletableIds.size())
                .failCount(errorMsgs.size())
                .errorMessages(errorMsgs)
                .build();
    }

    @Override
    public void forceDeleteImage(String imageId) {
        this.removeById(imageId);
    }

    @Override
    public Page<ImageInfoVo> listImages(ImageQueryDto request) {
        LambdaQueryWrapper<BlogImage> wrapper = new LambdaQueryWrapper<>();
        if(request.getIsTemporary() != null)
            wrapper.eq(BlogImage::getIsTemporary, request.getIsTemporary());
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(BlogImage::getOriginalName, request.getKeyword());
        }
        if (StringUtils.hasText(request.getUsageType())) {
            wrapper.eq(BlogImage::getUsageType, request.getUsageType());
        }
        if (StringUtils.hasText(request.getMimeType())) {
            wrapper.eq(BlogImage::getMimeType, request.getMimeType());
        }
        Page<BlogImage> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<BlogImage> imagePage = this.page(page, wrapper);

        List<BlogImage> list = imagePage.getRecords();
        List<ImageInfoVo> data = list.stream().map(item -> {
            ImageInfoVo curr = new ImageInfoVo();
            BeanUtils.copyProperties(item, curr);
            return curr;
        }).toList();
        Page<ImageInfoVo> res = new Page<>(request.getPageSize(), request.getPageNum(), imagePage.getTotal());
        res.setRecords(data);
        return res;

    }

    @Override
    public Map<String, String> uploadImageWithReference(MultipartFile file, Integer usageType, Long usageId)  {
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
                         .set(BlogImage::getIsTemporary, false)
                         .setSql("reference_count = reference_count + 1"); // 优雅地更新引用计数

            this.update(updateWrapper);
        }
    }

    @Override
    public List<ImageReferenceVo> imageReference(ImageReferenceDto request) {
        if(request == null || !StringUtils.hasText(request.getImageId())){
            log.info("imageId is empty");
            return null;
        }
        LambdaQueryWrapper<BlogImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogImage::getId, request.getImageId());
        
        // 必须在同一个 select 中指定所有字段，否则会被覆盖
        wrapper.select(BlogImage::getId, BlogImage::getUsageType, BlogImage::getUsageId);
        
        List<BlogImage> list = this.list(wrapper);

        return list.stream().map(item -> {
            ImageReferenceVo curr = new ImageReferenceVo();
            curr.setUsageId(item.getUsageId());
            curr.setUsageType(item.getUsageType());
            // 预览url
            curr.setSourceUrl(item.getUrl());
            // 后端回填标题：根据业务类型获取名称
            if ((ImageUsageType.ARTICLE_CONTENT.getType().equals(item.getUsageType()) ||
                ImageUsageType.ARTICLE_COVER.getType().equals(item.getUsageType()))
                && item.getUsageId() != null) {
                var article = articleService.getById(item.getUsageId());
                if (article != null) {
                    curr.setSourceTitle(article.getTitle());

                }
            }
            return curr;
        }).collect(Collectors.toList());
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