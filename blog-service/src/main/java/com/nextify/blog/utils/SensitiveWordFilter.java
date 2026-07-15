package com.nextify.blog.utils;

import cn.hutool.dfa.SensitiveUtil;
import com.nextify.blog.entity.SensitiveWord;
import com.nextify.blog.mapper.SensitiveWordMapper;
import com.nextify.blog.service.SensitiveWordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词过滤器（基于 DFA 算法 / Trie 树）
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    private static final List<String> fileNames =Arrays.asList("广告","色情","政治", "涉枪涉爆违法信息", "网址违禁");

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;


    /**
     * 刷新敏感词库
     * 未来可以改为从数据库或外部文件加载
     */
    public void refresh() {
        log.info("正在初始化/刷新敏感词过滤器...");
        
        // 从数据库获取所有词条
        List<SensitiveWord> list = sensitiveWordMapper.selectList(null);
        List<String> sensitiveWords = list.stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());

        SensitiveUtil.init(sensitiveWords);
        log.info("敏感词库加载完成，总计: {} 个词", sensitiveWords.size());
    }

    /**
     * 从 resource 目录下的文件加载敏感词并插入到数据库
     * 文件格式：每行一个敏感词，支持注释（#开头）
     * 文件路径：classpath:sensitive-words.txt
     */
    public void loadSensitiveWordsFromResources() {
        for(String fileName : fileNames){
            String filePath = fileName + ".txt";
            log.info("开始从资源文件加载敏感词: {}", filePath);

            List<SensitiveWord> sensitiveWords = new ArrayList<>();

            // 从 classpath 读取文件
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                if (is == null) {
                    log.warn("未找到敏感词文件: {}", filePath);
                    return;
                }

                String line;
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    line = line.trim();

                    // 跳过空行和注释（#开头）
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    // 解析行内容
                    // 支持格式：敏感词 或 敏感词|分类
                    String word = line;
                    String category = fileName;

                    if (line.contains("|")) {
                        String[] parts = line.split("\\|", 2);
                        word = parts[0].trim();
                        category = parts[1].trim();
                    }

                    // 验证敏感词
                    if (word.isEmpty()) {
                        log.warn("第 {} 行敏感词为空，跳过", lineNum);
                        continue;
                    }

                    SensitiveWord sw = new SensitiveWord();
                    sw.setWord(word);
                    sw.setCategory(category);
                    sensitiveWords.add(sw);
                }

                log.info("从文件读取到 {} 个敏感词", sensitiveWords.size());

            } catch (Exception e) {
                log.error("读取敏感词文件失败", e);
                return;
            }

            // 批量插入数据库
            if (!sensitiveWords.isEmpty()) {
                batchInsert(sensitiveWords);
            }
        }

    }
    
    /**
     * 批量插入敏感词到数据库（去重）
     * @param sensitiveWords 敏感词列表
     */
    private void batchInsert(List<SensitiveWord> sensitiveWords) {
        try {

            
            if (sensitiveWords.isEmpty()) {
                log.info("没有新的敏感词需要插入");
                return;
            }
            
            // 批量插入
            sensitiveWordService.insertBatch(sensitiveWords);
            log.info("成功插入 {} 个新敏感词", sensitiveWords.size());
            
            // 刷新敏感词过滤器
            refresh();
            
        } catch (Exception e) {
            log.error("批量插入敏感词失败", e);
        }
    }

    /**
     * 检查是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        return SensitiveUtil.containsSensitive(text);
    }

    /**
     * 替换文本中的敏感词，默认使用 '*' 替换
     * @param text 待替换文本
     * @return 替换后的文本
     */
    public String replaceSensitiveWord(String text) {
        return SensitiveUtil.sensitiveFilter(text);
    }


}