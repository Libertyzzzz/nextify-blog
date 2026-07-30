package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextify.blog.entity.AgentSystemPrompt;
import com.nextify.blog.mapper.AgentSystemPromptMapper;
import com.nextify.blog.service.PromptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PromptServiceImpl implements PromptService {

    @Resource
    private AgentSystemPromptMapper agentSystemPromptMapper;

    @Override
    public String getSystemPrompt(String contextKey) {
        // 优先获取活跃的、最新版本的系统提示词
        AgentSystemPrompt systemPrompt = agentSystemPromptMapper.selectOne(
                new QueryWrapper<AgentSystemPrompt>()
                        .eq("context_key", contextKey)
                        .eq("is_active", 1)
                        .orderByDesc("version") // 假设版本号是可比较的字符串或数字
                        .last("LIMIT 1")
        );
        if (systemPrompt != null) {
            // TODO: 支持 {site}、{user} 等占位符的替换
            return systemPrompt.getPromptText();
        }
        // 提供一个默认的系统提示词
        return "You are a helpful AI assistant.";
    }
}
