package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.dto.LlmMessage;
import com.nextify.blog.dto.LlmRequest; // 导入 LlmRequest
import com.nextify.blog.entity.AgentConversation;
import com.nextify.blog.entity.AgentMessage;
import com.nextify.blog.entity.AgentUsageLog;
import com.nextify.blog.mapper.AgentConversationMapper;
import com.nextify.blog.mapper.AgentMessageMapper;
import com.nextify.blog.mapper.AgentUsageLogMapper;
import com.nextify.blog.service.ConversationChatService;
import com.nextify.blog.service.LlmProvider; // 导入 LlmProvider
import com.nextify.blog.service.LlmRouterService; // 导入 LlmRouterService
import com.nextify.blog.service.PromptBuilderService;
import com.nextify.blog.service.QuotaService;
import com.nextify.blog.vo.AgentChatResponseVo;
import com.nextify.blog.vo.LlmResponse; // 导入 LlmResponse
import com.nextify.blog.vo.UsageVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // 导入 Map
import java.util.UUID;

@Service
public class ConversationChatServiceImpl implements ConversationChatService {

    @Resource
    private AgentConversationMapper agentConversationMapper;

    @Resource
    private AgentMessageMapper agentMessageMapper;

    @Resource
    private AgentUsageLogMapper agentUsageLogMapper;

    @Resource
    private QuotaService quotaService;

    @Resource
    private PromptBuilderService promptBuilderService;

    @Resource
    private LlmRouterService llmRouterService;

    @Resource
    private Map<String, LlmProvider> llmProviderMap;

    @Override
    @Transactional
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

        // 3) 拼装 Prompt
        List<LlmMessage> llmMessages = promptBuilderService.buildPromptMessages(userId, conversationId, request);

        // 4) 路由到合适 Provider / Model
        String model = llmRouterService.resolveModel(request.getAction());
        String providerName = llmRouterService.resolveProvider(model);
        LlmProvider llmProvider = llmProviderMap.get(providerName + "Impl"); // Spring 默认会将 bean 名称加上 Impl 后缀

        if (llmProvider == null) {
            throw new RuntimeException("LLM Provider not found for: " + providerName); // TODO: 抛出更友好的业务异常
        }

        // 5) 调用 LLM
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setMessages(llmMessages);
        llmRequest.setModel(model);
        llmRequest.setAction(request.getAction());
        // TODO: 设置 temperature, maxTokens 等参数

        long startTime = System.currentTimeMillis();
        LlmResponse llmResponse = llmProvider.chat(llmRequest);
        Integer latencyMs = (int) (System.currentTimeMillis() - startTime);

        // 使用 LLM 实际返回的 usage
        UsageVo llmUsage = llmResponse.getUsage();
        if (llmUsage == null) {
            llmUsage = new UsageVo(); // 确保不为空
            // TODO: 如果 LLM Provider 没有返回 usage，需要本地估算
        }

        // 检查并消耗配额
        if (!quotaService.checkAndConsumeQuota(userId, llmUsage.getTotalTokens())) {
            throw new RuntimeException("Quota exceeded for user: " + userId); // TODO: 抛出更友好的业务异常
        }

        // 6) 后处理 (TODO: 内容安全扫描、candidates 字段切分)
        // LLM Response 已经包含了 content 和 candidates

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

        userMessage.setStatus(1);
        // TODO: IP地址和UserAgent

        agentMessageMapper.insert(userMessage);

        // 写入助手消息
        AgentMessage assistantMessage = new AgentMessage();
        assistantMessage.setMessageId(llmResponse.getMessageId() != null ? llmResponse.getMessageId() : "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant");
        assistantMessage.setAction(request.getAction());
        assistantMessage.setContent(llmResponse.getContent());
        assistantMessage.setCandidates(llmResponse.getCandidates() != null ? String.join(",", llmResponse.getCandidates()) : null); // 将 List<String> 转换为逗号分隔的字符串
        assistantMessage.setPromptTokens(llmUsage.getPromptTokens());
        assistantMessage.setCompletionTokens(llmUsage.getCompletionTokens());
        assistantMessage.setTotalTokens(llmUsage.getTotalTokens());
        assistantMessage.setModel(llmResponse.getModel());
        assistantMessage.setLatencyMs(llmResponse.getLatencyMs() != null ? llmResponse.getLatencyMs() : latencyMs); // 优先使用 LLM 返回的延迟

        assistantMessage.setStatus(1);
        // TODO: error_reason, IP地址和UserAgent

        agentMessageMapper.insert(assistantMessage);

        // 写入用量审计日志
        AgentUsageLog usageLog = new AgentUsageLog();
        usageLog.setUserId(userId);
        usageLog.setConversationId(conversationId);
        usageLog.setMessageId(assistantMessage.getMessageId());
        usageLog.setAction(request.getAction());
        usageLog.setContextKey(request.getContext() != null ? request.getContext().getKey() : null);
        usageLog.setModel(llmResponse.getModel());
        usageLog.setProvider(llmResponse.getProvider());
        usageLog.setPromptTokens(llmUsage.getPromptTokens());
        usageLog.setCompletionTokens(llmUsage.getCompletionTokens());
        usageLog.setTotalTokens(llmUsage.getTotalTokens());
        usageLog.setLatencyMs(llmResponse.getLatencyMs() != null ? llmResponse.getLatencyMs() : latencyMs);
        usageLog.setStatus(llmResponse.getContent() != null && !llmResponse.getContent().isEmpty() ? 1 : -1); // 根据是否有内容判断成功或失败
        usageLog.setRequestAt(LocalDateTime.now());
        // TODO: error_code
        agentUsageLogMapper.insert(usageLog);

        // 更新会话信息
        conversation.setMessageCount(conversation.getMessageCount() + 2); // 用户消息 + 助手消息
        conversation.setLastMessagePreview(llmResponse.getContent() != null && llmResponse.getContent().length() > 200 ? llmResponse.getContent().substring(0, 200) + "..." : llmResponse.getContent());

        agentConversationMapper.updateById(conversation);

        // 8) 返回前端
        AgentChatResponseVo responseVo = new AgentChatResponseVo();
        responseVo.setMessageId(assistantMessage.getMessageId());
        responseVo.setRole("assistant");
        responseVo.setContent(assistantMessage.getContent());
        responseVo.setCandidates(llmResponse.getCandidates());
        responseVo.setAction(request.getAction());
        responseVo.setUsage(llmUsage);
        responseVo.setCreatedAt(assistantMessage.getCreateTime());

        return responseVo;
    }
}
