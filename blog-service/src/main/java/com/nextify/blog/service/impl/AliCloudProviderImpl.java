package com.nextify.blog.service.impl;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.nextify.blog.common.third.AliCloudComponent;
import com.nextify.blog.dto.LlmMessage;
import com.nextify.blog.dto.LlmRequest;
import com.nextify.blog.enums.AgentRoleEnum;
import com.nextify.blog.service.LlmProvider;
import com.nextify.blog.vo.LlmResponse;
import com.nextify.blog.vo.UsageVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service(value = "aliyun")
public class AliCloudProviderImpl implements LlmProvider {

    @Resource
    private AliCloudComponent aliCloudComponent;

    @Override
    public String getProviderName() {
        return "aliyun";
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 将 LlmMessage 转换为 Dashscope 的 Message 格式
        List<Message> dashscopeMessages = request.getMessages().stream()
                .map(llmMessage -> Message.builder()
                        .role(llmMessage.getRole().equals(AgentRoleEnum.USER.getValue()) ? Role.USER.getValue() :
                                llmMessage.getRole().equals(AgentRoleEnum.ASSISTANT.getValue()) ? Role.ASSISTANT.getValue() :
                                        Role.SYSTEM.getValue()) // 假设只有这三种角色
                        .content(llmMessage.getContent())
                        .build())
                .collect(Collectors.toList());

        // 2. 调用 AliCloudComponent
        GenerationResult result = aliCloudComponent.callWithDashscopeMessages(dashscopeMessages, request.getModel());

        long endTime = System.currentTimeMillis();
        Integer latencyMs = (int) (endTime - startTime);

        // 3. 将 GenerationResult 转换为 LlmResponse
        LlmResponse response = new LlmResponse();
        response.setProvider(getProviderName());
        response.setModel(request.getModel());
        response.setLatencyMs(latencyMs);

        Optional.ofNullable(result.getOutput())
                .map(GenerationOutput::getChoices)
                .filter(choices -> !choices.isEmpty())
                .map(choices -> choices.get(0))
                .ifPresent(choice -> {
                    response.setContent(choice.getMessage().getContent());
                    response.setFinishReason(choice.getFinishReason());
                    // 如果有多个候选回复，可以设置到 candidates
                    // response.setCandidates(...);
                });

        Optional.ofNullable(result.getUsage())
                .ifPresent(usage -> {
                    UsageVo usageVo = new UsageVo();
                    usageVo.setPromptTokens(usage.getInputTokens());
                    usageVo.setCompletionTokens(usage.getOutputTokens());
                    usageVo.setTotalTokens(usage.getTotalTokens());
                    response.setUsage(usageVo);
                });

        response.setMessageId("msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)); // Dashscope 结果中没有直接的 messageId，这里生成一个

        return response;
    }

    @Override
    public Flux<String> chatStream(LlmRequest request) {
        // TODO: Implement actual AliCloud streaming API call logic here if supported and needed
        // For now, return an empty flux or throw UnsupportedOperationException
        return Flux.empty();
    }
}
