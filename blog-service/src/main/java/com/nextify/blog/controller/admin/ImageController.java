package com.nextify.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.dto.ImageDeleteDto;
import com.nextify.blog.dto.ImageQueryDto;
import com.nextify.blog.dto.ImageReferenceDto;
import com.nextify.blog.service.ImageService;
import com.nextify.blog.vo.ImageDeleteResultVo;
import com.nextify.blog.vo.ImageInfoVo;
import com.nextify.blog.vo.ImageReferenceVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {

    @Resource
    private ImageService imageService;
    /**
     * 上传图片 并关联相关业务
     * @param file
     * @param usageType 使用类型
     * @param usageId 业务id
     */
    @PublicApi
    @PostMapping("/upload/with-reference")
    public Result<Map<String, String>> uploadWithReference(
        @RequestParam("file") MultipartFile file,
        @RequestParam("usageType") Integer usageType,
        @RequestParam(value = "usageId", required = false) Long usageId) {

        return Result.success(imageService.uploadImageWithReference(file, usageType, usageId));
    }

    /**
     * 查询图片列表
     */
    @GetMapping("/list")
    public Result<Page<ImageInfoVo>> listImages(ImageQueryDto request) {
        return Result.success(imageService.listImages(request));
    }

    /**
     * 查询图片引用信息
     */
    @GetMapping("/reference")
    public Result<List<ImageReferenceVo>> getImageReference(ImageReferenceDto request) {
        return Result.success(imageService.imageReference(request)) ;
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/delete")
    public Result<ImageDeleteResultVo> deleteImage(@RequestBody ImageDeleteDto request) {
        log.info("正在执行批量删除操作, 待删除数量: {}", request.getIds() != null ? request.getIds().size() : 0);
        return Result.success(imageService.deleteImage(request));
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
