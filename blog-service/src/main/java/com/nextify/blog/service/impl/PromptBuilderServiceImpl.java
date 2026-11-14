package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.dto.LlmMessage;
import com.nextify.blog.entity.AgentMessage;
import com.nextify.blog.enums.AgentRoleEnum;
import com.nextify.blog.mapper.AgentMessageMapper;
import com.nextify.blog.service.ContentService; // 导入 ContentService
import com.nextify.blog.service.PromptBuilderService;
import com.nextify.blog.service.PromptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map; // 导入 Map


@Service
public class PromptBuilderServiceImpl implements PromptBuilderService {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final Encoding ENCODING = REGISTRY.getEncoding(EncodingType.CL100K_BASE);


    @Resource
    private PromptService promptService;

    @Resource
    private AgentMessageMapper agentMessageMapper;

    @Resource // 注入 ContentService
    private ContentService contentService;

    @Resource
    private com.nextify.blog.service.RAGRetrieverService ragRetrieverService;

    // calculate token
    private int countTokens(String text) {
        return text != null ? ENCODING.countTokens(text) : 0;
    }

    @Override
    public List<LlmMessage> buildPromptMessages(Long userId, String conversationId, AgentChatRequest request) {
        List<LlmMessage> messages = new ArrayList<>();

        // 1. 添加 System Prompt（附带 RAG 检索增强）
        String systemPromptText = promptService.getSystemPrompt(request.getContext() != null ? request.getContext().getKey() : "generic");
        String ragContext = buildRAGContext(request.getMessage());
        if (ragContext != null && !ragContext.isEmpty()) {
            String enhancedSystemPrompt = (systemPromptText != null ? systemPromptText : "你是一个专业的博客助手。")
                    + "\n\n## 以下是你可以参考的知识库内容：\n"
                    + ragContext
                    + "\n\n**重要**：请优先参考上述知识库内容来回答用户问题。如果知识库中没有相关信息，请诚实告知用户。";
            messages.add(new LlmMessage(AgentRoleEnum.SYSTEM.getValue(), enhancedSystemPrompt));
            systemPromptText = enhancedSystemPrompt;
        } else if (systemPromptText != null && !systemPromptText.isEmpty()) {
            messages.add(new LlmMessage(AgentRoleEnum.SYSTEM.getValue(), systemPromptText));
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
                        .orderByDesc("create_time")
                        .last("LIMIT 20") // 假设最多加载最近20条消息
        );

        // 反转列表，从最旧的消息开始处理，但实际添加到prompt时是从新到旧
        Collections.reverse(historyMessages);

        List<LlmMessage> historyLlmMessages = new ArrayList<>();
        for (AgentMessage msg : historyMessages) {
            // 过滤掉当前请求的用户消息，因为它会单独添加
            if (msg.getRole().equals(AgentRoleEnum.USER.getValue()) && msg.getContent().equals(request.getMessage())) {
                continue;
            }

            LlmMessage llmMsg = new LlmMessage(msg.getRole(), msg.getContent());
            int msgTokens = countTokens(llmMsg.getContent());

            // 如果添加这条消息会超出预算，则停止
            // TODO: 更精确的预算控制，例如为LLM的回复预留空间
            if (currentTokens + msgTokens > maxTokensPerRequest * 0.8) { // 留20%给LLM回复
                // 在顶部插入提示
                messages.add(1, new LlmMessage(AgentRoleEnum.SYSTEM.getValue(), "更早的对话已省略..."));
                break;
            }
            historyLlmMessages.add(llmMsg);
            currentTokens += msgTokens;
        }
        messages.addAll(historyLlmMessages);


        // 3. 添加当前用户消息，并根据 context.payload 注入额外信息
        StringBuilder userPromptContentBuilder = new StringBuilder(request.getMessage());

        if (request.getContext() != null && request.getContext().getPayload() != null && !request.getContext().getPayload().isEmpty()) {
            String contextKey = request.getContext().getKey();
            Map<String, Object> payload = request.getContext().getPayload();

            // 根据 contextKey 处理不同的上下文
            if ("publish".equals(contextKey) || "post-detail".equals(contextKey)) {
                String articleId = (String) payload.get("articleId");
                if (articleId != null) {
                    Map<String, Object> articleContext = contentService.getArticleContext(articleId);
                    if (articleContext != null && !articleContext.isEmpty()) {
                        userPromptContentBuilder.append("\n\nRelevant Article Context:\n");
                        articleContext.forEach((key, value) ->
                                userPromptContentBuilder.append(key).append(": ").append(value).append("\n")
                        );
                    }
                }
            }
            // TODO: 可以根据其他 contextKey 添加更多逻辑，例如 dashboard / assessment
        }
        messages.add(new LlmMessage(AgentRoleEnum.USER.getValue(), userPromptContentBuilder.toString()));

        return messages;
    }

    /**
     * 构建 RAG 知识库上下文
     * @param userQuery 用户提问
     * @return 格式化的知识库上下文文本（检索失败时降级返回 null）
     */
    private String buildRAGContext(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return null;
        }
        try {
            // 从知识库检索最相关的 5 个片段
            List<com.nextify.blog.service.RAGRetrieverService.KnowledgeSnippet> snippets =
                    ragRetrieverService.retrieve(userQuery, 5);
            if (snippets == null || snippets.isEmpty()) {
                return null;
            }

            StringBuilder context = new StringBuilder();
            for (com.nextify.blog.service.RAGRetrieverService.KnowledgeSnippet snippet : snippets) {
                context.append("### 来源：《").append(snippet.articleTitle()).append("》\n")
                        .append(snippet.content()).append("\n\n");
            }
            return context.toString().trim();
        } catch (Exception e) {
            // RAG 检索异常降级，不阻断正常问答流程
            return null;
        }
    }
}
