package com.nextify.blog.enums;

import lombok.Getter;

@Getter
public enum UploadErrorEnum {
    // 客户端错误
    FILE_EMPTY("上传失败：文件内容为空", 4001),
    FILE_TOO_LARGE("上传失败：文件大小超限, 图片大小不超过50M", 4002),
    INVALID_FILE_TYPE("上传失败：不支持的文件格式", 4003),
    INVALID_PARAMETER("请求参数错误", 4000),

    // 服务端错误
    UPLOAD_FAILED("文件上传失败", 5000),
    DISK_IO_ERROR("磁盘写入异常", 5001),
    FILE_DELETE_FAILED("物理文件删除失败", 5002),
    REMOTE_ACCESS_ERROR("第三方存储调用异常", 5003),
    TIMEOUT("上传响应超时", 5004),
    VALIDATION_FAILURE("图片验证失败", 5005);

    private final String message;
    private final Integer code;

    UploadErrorEnum(String message, Integer code) {
        this.message = message;
        this.code = code;
    }
}
