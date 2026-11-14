package com.nextify.blog.controller.admin;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.dto.BlogTagAddRequest;
import com.nextify.blog.dto.BlogTagUpdateRequest;
import com.nextify.blog.service.BlogTagService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tags")
public class AdminBlogTagController {

    @Resource
    private BlogTagService tagService;

    /**
     * 新增标签
     * POST /admin/tags
     */
    @RequirePermission("dashboard:tag:add")
    @PostMapping
    public Result<Long> addTag(@Valid @RequestBody BlogTagAddRequest request) {
        return Result.success(tagService.addTag(request));
    }

    /**
     * 更新标签
     * PUT /admin/tags/{id}
     */
    @RequirePermission("dashboard:tag:edit")
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @Valid @RequestBody BlogTagUpdateRequest request) {
        tagService.updateTag(id, request);
        return Result.success();
    }

    /**
     * 删除标签
     * DELETE /admin/tags/{id}
     */
    @RequirePermission("dashboard:tag:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
