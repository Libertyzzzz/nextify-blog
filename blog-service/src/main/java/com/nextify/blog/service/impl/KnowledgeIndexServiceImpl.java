package com.nextify.blog.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.nextify.blog.entity.BlogArticle;
import com.nextify.blog.entity.KnowledgeChunk;
import com.nextify.blog.enums.PublishStatusEnum;
import com.nextify.blog.mapper.BlogArticleMapper;
import com.nextify.blog.mapper.KnowledgeChunkMapper;
import com.nextify.blog.service.EmbeddingService;
import com.nextify.blog.service.KnowledgeIndexService;
import com.nextify.blog.service.RAGRetrieverService;
import com.nextify.blog.utils.TextChunker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库索引服务实现
 * 负责将博客文章切块、向量化并入库
 */
@Slf4j
@Service
public class KnowledgeIndexServiceImpl implements KnowledgeIndexService {

    private static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    private static final Encoding ENCODING = REGISTRY.getEncoding(EncodingType.CL100K_BASE);

    @Resource
    private BlogArticleMapper blogArticleMapper;

    @Resource
    private KnowledgeChunkMapper knowledgeChunkMapper;

    @Resource
    private TextChunker textChunker;

    @Resource
    private EmbeddingService embeddingService;

    @Resource
    private RAGRetrieverService ragRetrieverService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void indexArticle(Long articleId) {
        log.info("开始索引文章到知识库: articleId={}", articleId);

        // 1. 查询文章
        BlogArticle article = blogArticleMapper.selectById(articleId);
        if (article == null) {
            log.warn("文章不存在，无法索引: articleId={}", articleId);
            return;
        }

        // 2. 获取文章内容（优先 content，回退到 contentHtml）
        String content = article.getContent();
        if (!StringUtils.hasText(content)) {
            content = article.getContentHtml();
        }
        if (!StringUtils.hasText(content)) {
            log.warn("文章内容为空，跳过索引: articleId={}", articleId);
            return;
        }

        // 3. 删除旧分片
        knowledgeChunkMapper.deleteByArticleId(articleId);

        // 4. 文本切块
        List<String> chunks = textChunker.chunk(content);
        if (CollectionUtils.isEmpty(chunks)) {
            log.warn("文章切块结果为空: articleId={}", articleId);
            return;
        }

        // 5. 批量向量化
        List<List<Float>> embeddings = embeddingService.embedBatch(chunks);
        if (CollectionUtils.isEmpty(embeddings) || embeddings.size() != chunks.size()) {
            log.error("文章向量化失败: articleId={}, chunksSize={}, embeddingsSize={}",
                    articleId, chunks.size(), embeddings != null ? embeddings.size() : 0);
            throw new IllegalStateException("文章向量化失败: articleId=" + articleId);
        }

        // 6. 构建并保存分片
        List<KnowledgeChunk> chunkEntities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setArticleId(articleId);
            chunk.setArticleTitle(article.getTitle());
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunk.setEmbeddingJson(JSONUtil.toJsonStr(embeddings.get(i)));
            chunk.setTokenCount(countTokens(chunks.get(i)));
            chunk.setStatus(1);
            chunk.setCreateTime(now);
            chunk.setUpdateTime(now);
            chunkEntities.add(chunk);
        }

        for (KnowledgeChunk entity : chunkEntities) {
            knowledgeChunkMapper.insert(entity);
        }

        // 7. 刷新内存检索缓存
        ragRetrieverService.refreshCache();
        log.info("文章成功索引到知识库: articleId={}, 分块数={}", articleId, chunks.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void indexAllArticles() {
        log.info("开始全量重建知识库索引...");

        List<BlogArticle> articles = blogArticleMapper.selectList(
                new LambdaQueryWrapper<BlogArticle>()
                        .eq(BlogArticle::getStatus, PublishStatusEnum.PUBLISHED.getCode())
        );

        log.info("查询到已发布文章 {} 篇待索引", articles.size());
        int successCount = 0;
        int failCount = 0;

        for (BlogArticle article : articles) {
            try {
                indexArticle(article.getId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("文章索引失败: articleId={}, title={}, error={}",
                        article.getId(), article.getTitle(), e.getMessage(), e);
            }
        }

        log.info("知识库全量索引完成: 成功={}, 失败={}", successCount, failCount);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void removeArticle(Long articleId) {
        int deleted = knowledgeChunkMapper.deleteByArticleId(articleId);
        ragRetrieverService.refreshCache();
        log.info("已从知识库移除文章分片: articleId={}, 删除分片数={}", articleId, deleted);
    }

    @Override
    public void refreshCache() {
        ragRetrieverService.refreshCache();
    }

    @Override
    public java.util.Map<String, Object> getKnowledgeStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        List<java.util.Map<String, Object>> list = knowledgeChunkMapper.countChunksGroupByArticle();
        java.util.Map<String, Integer> chunkMap = new java.util.HashMap<>();
        if (list != null) {
            for (java.util.Map<String, Object> row : list) {
                Object artIdObj = row.get("articleId");
                Object cntObj = row.get("chunkCount");
                if (artIdObj != null && cntObj != null) {
                    chunkMap.put(artIdObj.toString(), ((Number) cntObj).intValue());
                }
            }
        }
        Long totalChunks = knowledgeChunkMapper.countTotalValidChunks();
        stats.put("chunkCounts", chunkMap);
        stats.put("totalChunks", totalChunks != null ? totalChunks : 0);
        stats.put("indexedArticleCount", chunkMap.size());
        return stats;
    }

    private int countTokens(String text) {
        return text != null ? ENCODING.countTokens(text) : 0;
    }
}
