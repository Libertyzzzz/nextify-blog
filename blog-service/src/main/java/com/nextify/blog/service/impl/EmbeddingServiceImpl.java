package com.nextify.blog.service.impl;

import com.alibaba.dashscope.embeddings.TextEmbeddingResultItem;
import com.nextify.blog.common.properties.AIAssistantProperty;
import com.nextify.blog.common.third.AliCloudComponent;
import com.nextify.blog.service.EmbeddingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 阿里云 Dashscope Embedding 实现
 * 使用 text-embedding-v2 模型（1536 维）
 */
@Service
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    @Resource
    private AliCloudComponent aliCloudComponent;

    @Resource
    private AIAssistantProperty aiAssistantProperty;

    @Override
    public List<Float> embed(String text) {
        if (!CollectionUtils.isEmpty(Collections.singletonList(text))) {
            List<List<Float>> batchRes = embedBatch(Collections.singletonList(text));
            if (!CollectionUtils.isEmpty(batchRes)) {
                return batchRes.get(0);
            }
        }
        return List.of();
    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        if (CollectionUtils.isEmpty(texts)) {
            return Collections.emptyList();
        }

        // Dashscope 单批次支持最多 25 条，进行分批处理
        int batchSize = 25;
        List<List<Float>> results = new java.util.ArrayList<>(texts.size());

        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);

            List<TextEmbeddingResultItem> items = aliCloudComponent.textToEmbeddingBatch(batch);
            if (CollectionUtils.isEmpty(items)) {
                log.warn("Text to Embedding batch failed for size={}", batch.size());
                for (int k = 0; k < batch.size(); k++) {
                    results.add(Collections.emptyList());
                }
                continue;
            }

            for (TextEmbeddingResultItem item : items) {
                List<Double> doubleList = item.getEmbedding();
                if (CollectionUtils.isEmpty(doubleList)) {
                    results.add(Collections.emptyList());
                } else {
                    results.add(doubleList.stream().map(Double::floatValue).toList());
                }
            }
        }

        log.info("Embedding 向量化完成: 输入 {} 条文本，产出 {} 条向量", texts.size(), results.size());
        return results;
    }
}
