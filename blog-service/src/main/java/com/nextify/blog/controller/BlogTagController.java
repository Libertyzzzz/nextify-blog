package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.dto.BlogTagAddRequest;
import com.nextify.blog.dto.BlogTagUpdateRequest;
import com.nextify.blog.dto.TagQueryRequest;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.service.BlogArticleTagService;
import com.nextify.blog.service.BlogTagService;
import com.nextify.blog.vo.BlogTagVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class BlogTagController {

    @Resource
    private BlogArticleTagService articleTagService;
    @Resource
    private BlogTagService tagService;

    /**
     * 分页获取标签列表
     * GET /tags/page
     */
    @PublicApi
    @GetMapping("/page")
    public Result<Page<BlogTagVo>> getTags(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "20") long pageSize){

        return  Result.success(tagService.geAllTags(pageNum, pageSize));
    }

    /**
     * 根据ID列表获取标签信息
     * GET /tags?ids=1,2,3
     */
    @PublicApi
    @GetMapping()
    public Result<List<BlogTagVo>> getTagInfos(TagQueryRequest request) {
        if(request == null || request.getIds() == null || request.getIds().isEmpty())
            return Result.fail("未传入任何参数或ID列表为空");
        return Result.success(tagService.getTagInfos(request.getIds()));
    }

    /**
     * 根据ID获取标签详情
     * GET /tags/{id}
     */
    @PublicApi
    @GetMapping("/{id}")
    public Result<BlogTag> getTagById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    /**
     * 新增标签
     * POST /tags
     */
    @RequirePermission("content:tag:add")
    @PostMapping
    public Result<Long> addTag(@Valid @RequestBody BlogTagAddRequest request) {
        return Result.success(tagService.addTag(request));
    }

    /**
     * 更新标签
     * PUT /tags/{id}
     */
    @RequirePermission("content:tag:edit")
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @Valid @RequestBody BlogTagUpdateRequest request) {
        tagService.updateTag(id, request);
        return Result.success();
    }

    /**
     * 删除标签
     * DELETE /tags/{id}
     */
    @RequirePermission("content:tag:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
