package com.nextify.blog.service;

import java.util.List;

public interface RAGRetrieverService {

    /**
     * 检索与问题最相关的知识片段
     * @param query 用户提问
     * @param topK 返回前 K 条
     * @return 相关片段列表（按相似度降序）
     */
    List<KnowledgeSnippet> retrieve(String query, int topK);

    /**
     * 检索结果记录
     */
    record KnowledgeSnippet(
        Long articleId,
        String articleTitle,
        String content,
        double similarity
    ) {}
}
