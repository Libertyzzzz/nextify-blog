package com.nextify.blog.service.impl;

import com.nextify.blog.dto.LlmRequest;
import com.nextify.blog.service.LlmProvider;
import com.nextify.blog.vo.LlmResponse;
import com.nextify.blog.vo.UsageVo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
public class OpenAiProviderImpl implements LlmProvider {

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        // TODO: Implement actual OpenAI API call logic here
        // For now, simulate a response
        long startTime = System.currentTimeMillis();

        LlmResponse response = new LlmResponse();
        response.setContent("Simulated OpenAI response for: " + request.getMessages().get(request.getMessages().size() - 1).getContent());
        response.setCandidates(Collections.singletonList("Candidate 1 from OpenAI"));
        
        UsageVo usage = new UsageVo();
        usage.setPromptTokens(request.getMessages().stream().mapToInt(msg -> msg.getContent().length() / 4).sum()); // Rough estimate
        usage.setCompletionTokens(response.getContent().length() / 4); // Rough estimate
        usage.setTotalTokens(usage.getPromptTokens() + usage.getCompletionTokens());
        response.setUsage(usage);

        response.setModel(request.getModel());
        response.setProvider(getProviderName());
        response.setLatencyMs((int) (System.currentTimeMillis() - startTime));
        response.setMessageId("msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        response.setFinishReason("stop");

        return response;
    }

    @Override
    public Flux<String> chatStream(LlmRequest request) {
        // TODO: Implement actual OpenAI streaming API call logic here
        // For now, simulate a streaming response
        return Flux.just("event: delta\ndata: {\"delta\":\"Hello\"}\n",
                         "event: delta\ndata: {\"delta\":\" from\"}\n",
                         "event: delta\ndata: {\"delta\":\" OpenAI\"}\n",
                         "event: done\ndata: {\"messageId\":\"msg_simulated\",\"usage\":{\"totalTokens\":100}}\n");
    }
}
