package com.nextify.blog.service;

import com.nextify.blog.entity.BlogImage;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

public interface ImageService {
    void deleteImage(String imageId);

    void forceDeleteImage(String imageId);

    List<BlogImage> listImages();

    /**
     * 上传图片并关联业务实体
     * @param file 上传的文件
     * @param usageType 使用类型（例如 "article", "user_avatar"）
     * @param usageId 关联业务ID，可为空（表示临时图片）
     * @return 包含图片URL、路径、名称和ID的Map
     */
    Map<String, String> uploadImageWithReference(MultipartFile file, String usageType, Long usageId);

    /**
     * 更新图片的业务关联信息
     * @param imageIds 图片ID列表
     * @param articleId 关联的文章ID
     */
    void updateImageUsage(List<Long> imageIds, Long articleId);
}