package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.dto.ArticlePublishRequest;
import com.nextify.blog.service.BlogArticleService;
import com.nextify.blog.vo.ArticleDetailVO;
import com.nextify.blog.vo.ArticleListItemVO;
import com.nextify.blog.vo.ArticleTagDetailVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ArticleController {

    @Autowired
    private BlogArticleService articleService;

    @PublicApi
    @GetMapping("/articles")
    public Result<Page<ArticleListItemVO>> getArticles(@RequestParam(defaultValue = "1") long pageNum,
                                                        @RequestParam(defaultValue = "10") long pageSize,
                                                        @RequestParam (defaultValue = "1", required = false) Integer status){
        return Result.success(articleService.getHomeArticles(pageNum, pageSize, status));
    }

    /**
     * 搜索文章
     * GET /articles/search
     */
    @PublicApi
    @GetMapping("/articles/search")
    public Result<Page<ArticleListItemVO>> searchArticles(@RequestParam String keyword,
                                                          @RequestParam(defaultValue = "1") long pageNum,
                                                          @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(articleService.searchArticles(keyword, pageNum, pageSize));
    }

    @PublicApi
    @GetMapping("/articles/{id}")
    public Result<ArticleDetailVO> getDetail(@PathVariable Long id,
                                              @RequestParam(defaultValue = "1") Integer status ) {
        return Result.success(articleService.getArticleDetail(id, status));
    }

    @PostMapping("/admin/articles")
    public Result<Long> publish(@Validated @RequestBody ArticlePublishRequest request) {
        return Result.success(articleService.publishArticle(request));
    }

    @PutMapping("/admin/articles/{id}")
    public Result<Long> update(@PathVariable Long id, @Validated @RequestBody ArticlePublishRequest request) {
        return Result.success(articleService.updateArticle(id, request));
    }

    @DeleteMapping("/admin/articles/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }


    @PublicApi
    @GetMapping("/articles/tags/{id}")
    public Result<ArticleTagDetailVO> getArticleTags(@PathVariable  Long id) {
        return Result.success(articleService.getArticleTagDetail(id));
    }
}