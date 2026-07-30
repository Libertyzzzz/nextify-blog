package com.nextify.blog.service;

public interface AuditService {
    /**
     * 对文本内容进行敏感词过滤和清理。
     * @param content 原始文本内容
     * @return 清理后的文本内容
     */
    String cleanSensitiveInfo(String content);

    // TODO: 可以扩展其他审计功能，例如记录操作日志、数据脱敏等
}
