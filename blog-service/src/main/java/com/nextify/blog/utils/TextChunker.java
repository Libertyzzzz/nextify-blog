package com.nextify.blog.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本切块器
 * 将长文本按段落/句子切分成适合 Embedding 的小块
 *
 * 策略：
 * 1. 优先按段落分割（双换行）
 * 2. 超长段落按句子切分（标点符号）
 * 3. 块之间有重叠窗口，保持上下文连贯
 */
@Slf4j
@Component
public class TextChunker {

    /** 每块最大字符数（约 300-500 token） */
    private static final int MAX_CHUNK_SIZE = 800;

    /** 重叠字符数（保持上下文连贯） */
    private static final int OVERLAP_SIZE = 80;

    /**
     * 将文章内容切块
     * @param text 原始文本（Markdown 或纯文本）
     * @return 切块列表
     */
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // 第一步：按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // 如果段落本身就超长，按句子切
            if (trimmed.length() > MAX_CHUNK_SIZE) {
                splitLongParagraph(trimmed, chunks, currentChunk);
                continue;
            }

            // 当前块 + 新段落 → 超长？
            if (currentChunk.length() + trimmed.length() + 2 > MAX_CHUNK_SIZE) {
                // 保存当前块
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    // 重叠：保留当前块末尾的内容
                    String overlap = currentChunk.toString();
                    if (overlap.length() > OVERLAP_SIZE) {
                        overlap = overlap.substring(overlap.length() - OVERLAP_SIZE);
                    }
                    currentChunk = new StringBuilder(overlap);
                }
            }
            currentChunk.append(trimmed).append("\n\n");
        }

        // 最后一块
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        log.debug("文本切块完成: {} 字符 → {} 块", text.length(), chunks.size());
        return chunks;
    }

    /**
     * 处理超长段落：按句子切分
     */
    private void splitLongParagraph(String text, List<String> chunks, StringBuilder currentChunk) {
        // 按中英文句号切分
        String[] sentences = text.split("(?<=[。！？.!?])");
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (currentChunk.length() + trimmed.length() > MAX_CHUNK_SIZE) {
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
            }
            currentChunk.append(trimmed);
        }
    }
}

