package com.nextify.blog.service;

import java.util.List;

/**
 * 向量嵌入服务接口
 * 将文本转换为语义向量，用于 RAG 检索
 */
public interface EmbeddingService {

    /**
     * 将单段文本转为向量
     * @param text 输入文本
     * @return 1536维向量
     */
    List<Float> embed(String text);

    /**
     * 批量将多段文本转为向量
     * @param texts 文本列表
     * @return 向量列表
     */
    List<List<Float>> embedBatch(List<String> texts);

    /**
     * 计算向量的模（用于余弦相似度计算）
     * @param vector 向量
     * @return 模
     */
    default float norm(List<Float> vector) {
        float sum = 0f;
        for (Float v : vector) {
            sum += v * v;
        }
        return (float) Math.sqrt(sum);
    }

    /**
     * 计算两个向量的余弦相似度
     * @param a 向量A
     * @param b 向量B
     * @return 相似度 [-1, 1]，越大越相似
     */
    default float cosineSimilarity(List<Float> a, List<Float> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("向量维度不一致: " + a.size() + " vs " + b.size());
        }
        float dotProduct = 0f;
        float normA = 0f;
        float normB = 0f;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        float normProduct = (float) (Math.sqrt(normA) * Math.sqrt(normB));
        return normProduct == 0 ? 0 : dotProduct / normProduct;
    }
}