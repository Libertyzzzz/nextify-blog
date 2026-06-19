package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.dto.BlogCategoryAddRequest;
import com.nextify.blog.dto.BlogCategoryUpdateRequest;
import com.nextify.blog.entity.BlogCategory;
import com.nextify.blog.service.BlogCategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class BlogCategoryController {

    @Resource
    private BlogCategoryService blogCategoryService;

    /**
     * 获取所有分类列表
     * GET /categories/all
     */
    @GetMapping("/list")
    public Result<List<BlogCategory>> getAllCategories() {
        return Result.success(blogCategoryService.getAllCategories());
    }

    /**
     * 分页获取分类列表
     * GET /categories
     */
    @GetMapping
    public Result<Page<BlogCategory>> getCategoriesByPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(blogCategoryService.getCategoriesByPage(pageNum, pageSize));
    }

    /**
     * 根据ID获取分类详情
     * GET /categories/{id}
     */
    @GetMapping("/{id}")
    public Result<BlogCategory> getCategoryById(@PathVariable Long id) {
        return Result.success(blogCategoryService.getById(id));
    }

    /**
     * 新增分类
     * POST /categories
     */
    @PostMapping
    public Result<Long> addCategory(@Valid @RequestBody BlogCategoryAddRequest request) {
        return Result.success(blogCategoryService.addCategory(request));
    }

    /**
     * 更新分类
     * PUT /categories/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody BlogCategoryUpdateRequest request) {
        blogCategoryService.updateCategory(id, request);
        return Result.success();
    }

    /**
     * 删除分类
     * DELETE /categories/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        blogCategoryService.deleteCategory(id);
        return Result.success();
    }
}
