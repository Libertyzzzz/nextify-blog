package com.nextify.blog.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AgentChatRequest {
    // 1. 用户输入的消息正文
    // 例如："帮我给这篇文章想3个标题"
    private String message;

    // 2. 用户的意图或动作
    // 例如："generate_title", "polish_text", "chat"
    // 后端可以根据这个 action 调用不同的模型或 Prompt
    private String action;

    // 3. 上下文信息，让 AI 知道当前聊天的背景
    private Context context;

    // 4. 是否使用流式响应 (Server-Sent Events)
    // true: 后端会以流的形式一个字一个字地返回
    // false: 后端会等 AI 全部生成完再一次性返回
    private Boolean stream;

    @Data
    public static class Context {
        // 3.1 上下文的唯一标识
        // 例如："publish" (发布页), "post-detail" (文章详情页)
        // 后端可以根据这个 key 去数据库加载不同的系统提示词 (System Prompt)
        private String key;

        // 3.2 具体的上下文载荷，内容是动态的
        // 例如：在发布页，这里可以包含文章的 ID、标签、分类等
        // "payload": { "articleId": "art_001", "tags": ["微服务", "架构"] }
        private Map<String, Object> payload;
    }
}
