package com.nextify.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextify.blog.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识库分片 Mapper
 */
@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    /**
     * 计算两个向量的余弦相似度（使用 MySQL JSON 函数）
     * @param queryEmbedding 查询向量
     * @param topK 返回前K条
     * @return 相似度最高的K个分片
     */
//    @Select("SELECT id, article_id, article_title, chunk_index, content, " +
//        "1 - ( " +
//        "  (CAST(JSON_EXTRACT(embedding_json, '$[0]') AS DECIMAL(20,10)) * #{queryEmbedding}[0]) + " +
//        "  (CAST(JSON_EXTRACT(embedding_json, '$[1]') AS DECIMAL(20,10)) * #{queryEmbedding}[1]) + " +
//        "  ... " +  // 此处需要动态生成，见下方说明
//        ") / " +
//        "  (SQRT(CAST(JSON_EXTRACT(embedding_json, '$[0]') AS DECIMAL(20,10)) * CAST(JSON_EXTRACT(embedding_json, '$[0]') AS DECIMAL(20,10)) + ...) * " +
//        "   SQRT(#{queryNorm})) " +
//        ") AS similarity " +
//        "FROM knowledge_chunk " +
//        "WHERE status = 1 " +
//        "ORDER BY similarity DESC " +
//        "LIMIT #{topK}")
//    List<KnowledgeChunk> findTopSimilar(@Param("queryEmbedding") List<Float> queryEmbedding,
//                                        @Param("queryNorm") float queryNorm,
//                                        @Param("topK") int topK);


    /**
     * 查询所有有效的知识分片（应用层做余弦相似度计算）
     */
    @Select("SELECT * FROM knowledge_chunk WHERE status = 1")
    List<KnowledgeChunk> findAllValid();

    /**
     * 根据文章ID查询所有分片
     */
    @Select("SELECT * FROM knowledge_chunk WHERE article_id = #{articleId}")
    List<KnowledgeChunk> findByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据文章ID删除所有分片
     */
    @Select("DELETE FROM knowledge_chunk WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Long articleId);
}