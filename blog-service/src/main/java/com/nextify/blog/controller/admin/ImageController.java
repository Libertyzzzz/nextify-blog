package com.nextify.blog.controller.admin;

import com.nextify.blog.common.Result;
import com.nextify.blog.entity.BlogImage;
import com.nextify.blog.service.ImageService;
import com.nextify.blog.vo.ImageInfoVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/upload")
public class ImageController {

    @Resource
    private ImageService imageService;
    /**
     * 上传图片 并关联相关业务
     * @param file
     * @param usageType 使用类型
     * @param usageId 业务id
     */
    @PostMapping("/image/with-reference")
    public Result<Map<String, String>> uploadWithReference(
        @RequestParam("file") MultipartFile file,
        @RequestParam("usageType") String usageType,
        @RequestParam(value = "usageId", required = false) Long usageId) {

        return Result.success(imageService.uploadImageWithReference(file, usageType, usageId));
    }

    /**
     * 查询图片列表
     */
    @GetMapping("/list")
    public Result<List<ImageInfoVo>> listImages() {
        List<BlogImage> images = imageService.listImages();
        return Result.success(null);
    }

    /**
     * 查询图片引用信息
     */
    @GetMapping("/{imageId}")
    public Result<ImageInfoVo> getImageReference(@PathVariable("imageId") String imageId) {
        return null;
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/{imageId}")
    public Result<Void> deleteImage(@PathVariable("imageId") String imageId) {
        imageService.deleteImage(imageId);
        return Result.success();
    }

    /**
     * 强制删除
     */
    @DeleteMapping("/force/{imageId}")
    public Result<Void> forceDeleteImage(@PathVariable("imageId") String imageId) {
        imageService.forceDeleteImage(imageId);
        return Result.success();
    }
}
