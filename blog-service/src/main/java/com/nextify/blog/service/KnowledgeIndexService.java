package com.nextify.blog.service;

/**
 * 知识库索引服务接口
 * 负责将博客文章处理并入库为向量知识库
 */
public interface KnowledgeIndexService {

    /**
     * 将指定文章加入知识库
     * @param articleId 文章ID
     */
    void indexArticle(Long articleId);

    /**
     * 批量索引所有已发布文章
     */
    void indexAllArticles();

    /**
     * 从知识库移除指定文章
     * @param articleId 文章ID
     */
    void removeArticle(Long articleId);

    /**
     * 刷新检索缓存（新增/删除操作后调用）
     */
    void refreshCache();
}

