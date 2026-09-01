package com.nextify.blog.service.impl;

import cn.hutool.json.JSONUtil;
import com.nextify.blog.entity.KnowledgeChunk;
import com.nextify.blog.mapper.KnowledgeChunkMapper;
import com.nextify.blog.service.EmbeddingService;
import com.nextify.blog.service.RAGRetrieverService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * RAG 检索器实现
 * 使用 Embedding + 余弦相似度 从知识库中检索相关文档
 */
@Slf4j
@Service
public class RAGRetrieverServiceImpl implements RAGRetrieverService {

    @Resource
    private EmbeddingService embeddingService;

    @Resource
    private KnowledgeChunkMapper knowledgeChunkMapper;

    /**
     * 缓存：所有有效的知识分片（避免每次检索都查库）
     * key = chunkId, value = KnowledgeChunk（含解析后的向量）
     */
    private volatile Map<Long, KnowledgeChunk> chunkCache;

    /**
     * 缓存的向量数据（并行数组，提升检索性能）
     */
    private volatile List<float[]> cachedEmbeddings;
    private volatile List<KnowledgeChunk> cachedChunks;

    /**
     * 余弦相似度阈值
     */
    private static final float SIMILARITY_THRESHOLD = 0.3f;

    @Override
    public List<KnowledgeSnippet> retrieve(String query, int topK) {

        // 1. 对用户提问进行向量化
        List<Float> queryEmbedding = embeddingService.embed(query);
        float queryNorm = embeddingService.norm(queryEmbedding);

        // 2. 获取所有有效的知识分片
        List<KnowledgeChunk> allChunks = loadAllChunks();
        if (allChunks.isEmpty()) {
            log.warn("知识库为空，无法检索: {}", query);
            return List.of();
        }

        // 3.计算余弦相似度
        List<KnowledgeSnippet> res = new ArrayList<>();
        float[] queryVec = toFloatArray(queryEmbedding);

        for(int i = 0; i < cachedChunks.size(); i++){
            float similarity = cosineSimilarity(queryVec, cachedEmbeddings.get(i), queryNorm);
            if(similarity > SIMILARITY_THRESHOLD){
                KnowledgeChunk chunk = cachedChunks.get(i);
                res.add(new KnowledgeSnippet(
                    chunk.getArticleId(),
                    chunk.getArticleTitle(),
                    chunk.getContent(),
                    similarity
                ));
            }
        }

        // 4. 按照相似度倒序排列 取topK
        return res.stream()
            .sorted(new Comparator<KnowledgeSnippet>() {
                @Override
                public int compare(KnowledgeSnippet o1, KnowledgeSnippet o2) {
                    return Double.compare(o2.similarity(), o1.similarity());
                }
            })
            .limit(topK)
            .toList();
    }

    /**
     * 刷新缓存（从数据库重新加载所有分片）
     */
    public synchronized void refreshCache() {
        List<KnowledgeChunk> chunks = knowledgeChunkMapper.findAllValid();
        List<float[]> embeddings = new ArrayList<>(chunks.size());

        for (KnowledgeChunk chunk : chunks) {
            List<Float> vector = JSONUtil.toList(chunk.getEmbeddingJson(), Float.class);
            embeddings.add(toFloatArray(vector));
        }

        this.cachedChunks = chunks;
        this.cachedEmbeddings = embeddings;
        log.info("知识库缓存已刷新: {} 个分片", chunks.size());
    }

    /**
     * 加载所有有效的知识分片（带缓存）
     */
    private List<KnowledgeChunk> loadAllChunks() {
        if (cachedChunks != null) {
            return cachedChunks;
        }
        synchronized (this) {
            if (cachedChunks == null) {
                refreshCache();
            }
        }
        return cachedChunks;
    }

    /**
     * 计算余弦相似度（使用预计算的模）
     */
    private float cosineSimilarity(float[] a, float[] b, float normA) {
        float dotProduct = 0f;
        float normB = 0f;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normB += b[i] * b[i];
        }
        float normProduct = normA * (float) Math.sqrt(normB);
        return normProduct == 0 ? 0 : dotProduct / normProduct;
    }

    /**
     * List<Float> → float[]
     */
    private float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
