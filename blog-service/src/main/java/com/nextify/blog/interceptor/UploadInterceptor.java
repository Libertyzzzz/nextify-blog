package com.nextify.blog.interceptor;

import com.alibaba.fastjson2.JSON;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.ValidationResult;
import com.nextify.blog.common.validator.FileValidator;

import com.nextify.blog.common.validator.FileValidatorFactory;
import com.nextify.blog.common.validator.ImageFileValidator;
import com.nextify.blog.common.validator.VideoFileValidator;
import com.nextify.blog.enums.UploadErrorEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;


import java.io.IOException;

/**
 * 通用上传拦截器
 * 支持图片、视频等多种文件类型的前置校验
 * 设计原则：开闭原则（对扩展开放，对修改关闭）
 */
@Slf4j
@Component
public class UploadInterceptor implements HandlerInterceptor {

    @Resource
    private FileValidatorFactory validatorRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 检查是否是文件上传请求
        if (!isMultipartRequest(request)) {
            return true; // 非上传请求直接放行
        }

        // 2. 转换为 MultipartHttpServletRequest
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        // 3. 获取上传的文件
        MultipartFile file = multipartRequest.getFile("file");
        if (file == null || file.isEmpty()) {
            sendErrorResponse(response, UploadErrorEnum.FILE_EMPTY.getMessage());
            return false;
        }

        // 4. 根据请求路径判断文件类型
        String requestPath = request.getRequestURI();
        String fileType = determineFileType(requestPath);

        // 5. 获取对应的校验器并执行校验
        FileValidator validator = validatorRegistry.getInstance(fileType);
        ValidationResult validationResult = validator.validate(file);

        if (!validationResult.isValid()) {
            sendErrorResponse(response, validationResult.getMessage());
            return false;
        }

        log.info("文件上传校验通过: {}, type: {}, size: {} bytes",
            file.getOriginalFilename(), fileType, file.getSize());
        return true;
    }

    /**
     * 根据请求路径判断文件类型
     * 示例：/admin/upload/image → image
     *      /admin/upload/video → video
     */
    private String determineFileType(String requestPath) {
        if (requestPath.contains("/image")) {
            return "image";
        } else if (requestPath.contains("/video")) {
            return "video";
        }
        return "unknown";
    }

    /**
     * 判断是否是文件上传请求
     */
    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        Result<Void> result = Result.fail(message);
        String json = JSON.toJSONString(result);
        response.getWriter().write(json);
    }









}