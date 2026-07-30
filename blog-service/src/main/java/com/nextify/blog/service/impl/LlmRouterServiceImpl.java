package com.nextify.blog.service.impl;

import com.nextify.blog.service.LlmRouterService;
import org.springframework.stereotype.Service;

/**
 * LLM 路由服务实现类。
 * 负责根据业务动作 (action) 决定使用哪个 LLM 模型，以及该模型由哪个 Provider 提供。
 *
 * 变更说明：
 * 1. `resolveModel` 方法：
 *    - 对于 "generate_title" 动作，现在路由到 "qwen-plus" 模型。理由是写作工具类任务通常需要更高质量的生成能力。
 *    - 对于 "chat" 动作（以及默认情况），现在路由到 "qwen-turbo" 模型。理由是纯聊天任务对成本和响应速度更敏感，经济型模型更合适。
 * 2. `resolveProvider` 方法：
 *    - 新增了对以 "qwen" 开头的模型的判断，将其路由到 "aliyun" Provider。这是为了集成新创建的 `AliCloudProviderImpl`。
 *    - 默认 Provider 也改为了 "aliyun"，以优先使用阿里云通义千问服务。
 */
@Service
public class LlmRouterServiceImpl implements LlmRouterService {

    @Override
    public String resolveModel(String action) {
        // TODO: 根据 action 路由到不同的模型
        // 示例逻辑：
        if ("generate_title".equals(action)) {
            return "qwen -plus"; // 写作工具使用高质量模型，例如通义千问增强版
        } else if ("chat".equals(action)) {
            return "qwen-turbo"; // 纯聊天使用经济型模型，例如通义千问标准版
        }
        return "qwen-turbo"; // 默认模型
    }

    @Override
    public String resolveProvider(String model) {
        // TODO: 根据模型路由到不同的 Provider
        // 示例逻辑：
        if (model.startsWith("gpt")) {
            return "openai";
        } else if (model.startsWith("deepseek")) {
            return "deepseek";
        } else if (model.startsWith("qwen")) { // 通义千问模型
            return "aliyun";
        }
        return "aliyun"; // 默认 Provider
    }
}