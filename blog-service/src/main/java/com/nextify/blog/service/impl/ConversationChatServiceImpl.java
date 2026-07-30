package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.entity.AgentConversation;
import com.nextify.blog.entity.AgentMessage;
import com.nextify.blog.mapper.AgentConversationMapper;
import com.nextify.blog.mapper.AgentMessageMapper;
import com.nextify.blog.service.ConversationChatService;
import com.nextify.blog.vo.AgentChatResponseVo;
import com.nextify.blog.vo.UsageVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConversationChatServiceImpl implements ConversationChatService {

    @Resource
    private AgentConversationMapper agentConversationMapper;

    @Resource
    private AgentMessageMapper agentMessageMapper;

    @Override
    public AgentChatResponseVo chat(String conversationId, AgentChatRequest request) {
        // TODO: 1) 鉴权 & 限流 (假设 userId 已获取)
        Long userId = 1L; // 暂时硬编码为1L，后续从认证信息中获取

        // 2) 解析 conversation - 检查会话是否存在
        AgentConversation conversation = agentConversationMapper.selectOne(
                new QueryWrapper<AgentConversation>().eq("conversation_id", conversationId).eq("user_id", userId)
        );

        if (conversation == null) {
            // TODO: 抛出业务异常，会话不存在
            throw new RuntimeException("Conversation not found.");
        }

        // 3) 拼装 Prompt (TODO: 复杂逻辑，待实现)
        // system prompt = agent_system_prompt[context.key] + 注入当前博客信息 + 注入用户角色
        // history = 最近 N 条消息 (受 token 预算限制)
        // user prompt = message + context.payload (当前文章/数据快照)

        // 4) 路由到合适 Provider / Model (TODO: 复杂逻辑，待实现)
        String model = "default-model"; // 示例模型名

        // 5) 调用 LLM (TODO: 实际调用 LLM API)
        // 模拟 LLM 响应
        String llmResponseContent = "这是对您消息 '" + request.getMessage() + "' 的回复。";
        UsageVo llmUsage = new UsageVo();
        llmUsage.setPromptTokens(100);
        llmUsage.setCompletionTokens(50);
        llmUsage.setTotalTokens(150);

        // 6) 后处理 (TODO: 内容安全扫描、candidates 字段切分、估算 token 消耗)

        // 7) 写库
        // 写入用户消息
        AgentMessage userMessage = new AgentMessage();
        userMessage.setMessageId("msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        userMessage.setConversationId(conversationId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setAction(request.getAction());
        userMessage.setContent(request.getMessage());
        userMessage.setContextPayload(request.getContext() != null && request.getContext().getPayload() != null ? request.getContext().getPayload().toString() : null); // 简单转换为字符串
        userMessage.setCreatedAt(LocalDateTime.now());
        userMessage.setStatus(1);
        // TODO: IP地址和UserAgent

        agentMessageMapper.insert(userMessage);

        // 写入助手消息
        AgentMessage assistantMessage = new AgentMessage();
        assistantMessage.setMessageId("msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant");
        assistantMessage.setAction(request.getAction());
        assistantMessage.setContent(llmResponseContent);
        assistantMessage.setPromptTokens(llmUsage.getPromptTokens());
        assistantMessage.setCompletionTokens(llmUsage.getCompletionTokens());
        assistantMessage.setTotalTokens(llmUsage.getTotalTokens());
        assistantMessage.setModel(model);
        assistantMessage.setCreatedAt(LocalDateTime.now());
        assistantMessage.setStatus(1);
        // TODO: candidates, latency_ms, error_reason, IP地址和UserAgent

        agentMessageMapper.insert(assistantMessage);

        // 更新会话信息
        conversation.setMessageCount(conversation.getMessageCount() + 2); // 用户消息 + 助手消息
        conversation.setLastMessagePreview(llmResponseContent.length() > 200 ? llmResponseContent.substring(0, 200) + "..." : llmResponseContent);
        conversation.setUpdatedAt(LocalDateTime.now());
        agentConversationMapper.updateById(conversation);

        // TODO: agent_usage_log (1 行)
        // TODO: agent_quota: used_tokens += N (原子更新 / Redis 预扣)

        // 8) 返回前端
        AgentChatResponseVo responseVo = new AgentChatResponseVo();
        responseVo.setMessageId(assistantMessage.getMessageId());
        responseVo.setRole("assistant");
        responseVo.setContent(llmResponseContent);
        responseVo.setAction(request.getAction());
        responseVo.setUsage(llmUsage);
        responseVo.setCreatedAt(assistantMessage.getCreatedAt());

        return responseVo;
    }
}
