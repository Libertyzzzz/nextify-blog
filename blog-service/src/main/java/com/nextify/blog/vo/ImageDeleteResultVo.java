package com.nextify.blog.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ImageDeleteResultVo {
    private Integer successCount;
    private Integer failCount;
    /** 被拒绝删除的图片ID及其原因（例如：正在被文章《XXX》引用） */
    private List<String> errorMessages;
}