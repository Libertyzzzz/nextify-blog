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

    private AliCloudComponent aliCloudComponent;
    @Resource
    private AIAssistantProperty aiAssistantProperty;




    @Override
    public List<Float> embed(String text) {
        List<TextEmbeddingResultItem> textEmbeddingResultItems = aliCloudComponent.textToEmbedding(text);
        if(CollectionUtils.isEmpty(textEmbeddingResultItems)){
            log.info("Text to Embedding failed");
            return List.of();
        }
        List<Double> res = textEmbeddingResultItems.get(0).getEmbedding();
        if(CollectionUtils.isEmpty(res)){
            return List.of();
        }
        return res.stream()
            .map(Double::floatValue)
            .toList();

    }

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        return List.of();
    }
}
