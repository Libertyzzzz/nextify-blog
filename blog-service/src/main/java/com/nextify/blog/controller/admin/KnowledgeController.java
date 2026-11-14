package com.nextify.blog.controller.admin;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.service.KnowledgeIndexService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

/**
 * 知识库管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/knowledge")
public class KnowledgeController {

    @Resource
    private KnowledgeIndexService knowledgeIndexService;

    /**
     * 将指定文章切块向量化并加入知识库
     */
    @RequirePermission("dashboard:knowledge:index")
    @PostMapping("/index")
    public Result<Map<String, Object>> indexArticle(@RequestParam Long articleId) {
        knowledgeIndexService.indexArticle(articleId);

        return Result.success(Map.of("articleId", articleId, "status", "indexed"));
    }

    /**
     * 全量重新索引所有已发布文章
     */
    @RequirePermission("dashboard:knowledge:index")
    @PostMapping("/index-all")
    public Result<Map<String, Object>> indexAllArticles() {
        knowledgeIndexService.indexAllArticles();
        return Result.success(Map.of("status", "completed"));
    }

    /**
     * 从知识库中移除指定文章
     */
    @RequirePermission("dashboard:knowledge:remove")
    @DeleteMapping("/remove")
    public Result<Map<String, Object>> removeArticle(@RequestParam Long articleId) {
        knowledgeIndexService.removeArticle(articleId);
        return Result.success(Map.of("articleId", articleId, "status", "removed"));
    }

    /**
     * 刷新内存中的向量检索缓存
     */
    @RequirePermission("dashboard:knowledge:refresh")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshCache() {
        knowledgeIndexService.refreshCache();
        return Result.success(Map.of("status", "refreshed"));
    }

    /**
     * 获取知识库切片统计与每篇文章分块数
     */
    @RequirePermission("dashboard:knowledge:index")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getKnowledgeStats() {
        return Result.success(knowledgeIndexService.getKnowledgeStats());
    }
}
