package com.nextify.blog.controller.admin;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.dto.BlogCategoryAddRequest;
import com.nextify.blog.dto.BlogCategoryUpdateRequest;
import com.nextify.blog.service.BlogCategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
public class AdminBlogCategoryController {

    @Resource
    private BlogCategoryService blogCategoryService;

    /**
     * 新增分类
     * POST /admin/categories
     */
    @RequirePermission("dashboard:category:add")
    @PostMapping
    public Result<Long> addCategory(@Valid @RequestBody BlogCategoryAddRequest request) {
        return Result.success(blogCategoryService.addCategory(request));
    }

    /**
     * 更新分类
     * PUT /admin/categories/{id}
     */
    @RequirePermission("dashboard:category:edit")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody BlogCategoryUpdateRequest request) {
        blogCategoryService.updateCategory(id, request);
        return Result.success();
    }

    /**
     * 删除分类
     * DELETE /admin/categories/{id}
     */
    @RequirePermission("dashboard:category:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        blogCategoryService.deleteCategory(id);
        return Result.success();
    }
}
