package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库分片实体类
 * 对应表：knowledge_chunk
 * 每一行是一段向量化的文本，用于 RAG 语义检索
 */
@Data
@TableName("knowledge_chunk")
public class  KnowledgeChunk implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联博客文章ID */
    @TableField("article_id")
    private Long articleId;

    /** 文章标题（冗余存储，便于检索结果直接展示） */
    @TableField("article_title")
    private String articleTitle;

    /** 在文章中的块序号（从0开始） */
    @TableField("chunk_index")
    private Integer chunkIndex;

    /** 切块后的文本内容 */
    @TableField("content")
    private String content;

    /** 向量数据（JSON数组格式，存储1536维的float数组） */
    @TableField("embedding_json")
    private String embeddingJson;

    /** 该块的Token数量 */
    @TableField("token_count")
    private Integer tokenCount;

    /** 状态：1-有效 0-已删除 */
    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}