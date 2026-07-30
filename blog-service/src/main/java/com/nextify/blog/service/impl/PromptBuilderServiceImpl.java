package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.dto.LlmMessage;
import com.nextify.blog.entity.AgentMessage;
import com.nextify.blog.mapper.AgentMessageMapper;
import com.nextify.blog.service.PromptBuilderService;
import com.nextify.blog.service.PromptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PromptBuilderServiceImpl implements PromptBuilderService {

    @Resource
    private PromptService promptService;

    @Resource
    private AgentMessageMapper agentMessageMapper;

    // 假设一个简单的token计算方法，实际应使用tiktoken等库
    private int countTokens(String text) {
        return text != null ? text.length() / 4 : 0; // 粗略估算，1个汉字或4个英文字符约等于1 token
    }

    @Override
    public List<LlmMessage> buildPromptMessages(Long userId, String conversationId, AgentChatRequest request) {
        List<LlmMessage> messages = new ArrayList<>();

        // 1. 添加 System Prompt
        String systemPromptText = promptService.getSystemPrompt(request.getContext() != null ? request.getContext().getKey() : "generic");
        if (systemPromptText != null && !systemPromptText.isEmpty()) {
            messages.add(new LlmMessage("system", systemPromptText));
        }

        // 2. 添加历史消息 (从后向前累加，直到超过预算)
        // TODO: max_tokens_per_request 应该从配置或模型信息中获取
        int maxTokensPerRequest = 4096;
        int currentTokens = countTokens(systemPromptText) + countTokens(request.getMessage()); // 初始token预算

        // 加载最近的历史消息
        List<AgentMessage> historyMessages = agentMessageMapper.selectList(
                new QueryWrapper<AgentMessage>()
                        .eq("conversation_id", conversationId)
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
                        .last("LIMIT 20") // 假设最多加载最近20条消息
        );

        // 反转列表，从最旧的消息开始处理，但实际添加到prompt时是从新到旧
        Collections.reverse(historyMessages);

        List<LlmMessage> historyLlmMessages = new ArrayList<>();
        for (AgentMessage msg : historyMessages) {
            // 过滤掉当前请求的用户消息，因为它会单独添加
            if (msg.getRole().equals("user") && msg.getContent().equals(request.getMessage())) {
                continue;
            }

            LlmMessage llmMsg = new LlmMessage(msg.getRole(), msg.getContent());
            int msgTokens = countTokens(llmMsg.getContent());

            // 如果添加这条消息会超出预算，则停止
            // TODO: 更精确的预算控制，例如为LLM的回复预留空间
            if (currentTokens + msgTokens > maxTokensPerRequest * 0.8) { // 留20%给LLM回复
                // 在顶部插入提示
                messages.add(1, new LlmMessage("system", "更早的对话已省略..."));
                break;
            }
            historyLlmMessages.add(llmMsg);
            currentTokens += msgTokens;
        }
        messages.addAll(historyLlmMessages);


        // 3. 添加当前用户消息
        String userPromptContent = request.getMessage();
        if (request.getContext() != null && request.getContext().getPayload() != null && !request.getContext().getPayload().isEmpty()) {
            // TODO: 根据context.key和payload构建更复杂的user prompt
            userPromptContent += "\n\nContext: " + request.getContext().getPayload().toString();
        }
        messages.add(new LlmMessage("user", userPromptContent));

        return messages;
    }
}
