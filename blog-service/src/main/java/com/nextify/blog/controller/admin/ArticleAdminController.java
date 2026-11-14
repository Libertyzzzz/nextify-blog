package com.nextify.blog.controller.admin;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.dto.ArticlePublishRequest;
import com.nextify.blog.service.BlogArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/articles")
public class ArticleAdminController {

    @Autowired
    private BlogArticleService articleService;

    @RequirePermission("dashboard:article:add")
    @PostMapping
    public Result<Long> publish(@Validated @RequestBody ArticlePublishRequest request) {
        return Result.success(articleService.publishArticle(request));
    }

    @RequirePermission("dashboard:article:edit")
    @PutMapping("/{id}")
    public Result<Long> update(@PathVariable Long id, @Validated @RequestBody ArticlePublishRequest request) {
        return Result.success(articleService.updateArticle(id, request));
    }

    @RequirePermission("dashboard:article:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }
}
