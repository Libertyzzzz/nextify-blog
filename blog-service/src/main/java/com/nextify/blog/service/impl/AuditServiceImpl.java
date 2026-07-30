package com.nextify.blog.service.impl;

import com.nextify.blog.service.AuditService;
import com.nextify.blog.utils.SensitiveWordFilter; // 假设存在此工具类
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    @Resource
    private SensitiveWordFilter sensitiveWordFilter; // 注入敏感词过滤器

    @Override
    public String cleanSensitiveInfo(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        // 使用敏感词过滤器进行替换
        return sensitiveWordFilter.replaceSensitiveWord(content);
    }
}
