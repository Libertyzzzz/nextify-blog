package com.nextify.blog.service.impl;

import com.nextify.blog.service.LlmRouterService;
import org.springframework.stereotype.Service;

@Service
public class LlmRouterServiceImpl implements LlmRouterService {

    @Override
    public String resolveModel(String action) {
        // TODO: 根据 action 路由到不同的模型
        // 示例逻辑：
        if ("generate_title".equals(action)) {
            return "gpt-4o"; // 写作工具使用高质量模型
        } else if ("chat".equals(action)) {
            return "gpt-4o-mini"; // 纯聊天使用经济型模型
        }
        return "gpt-4o-mini"; // 默认模型
    }

    @Override
    public String resolveProvider(String model) {
        // TODO: 根据模型路由到不同的 Provider
        // 示例逻辑：
        if (model.startsWith("gpt")) {
            return "openai";
        } else if (model.startsWith("deepseek")) {
            return "deepseek";
        }
        return "openai"; // 默认 Provider
    }
}
