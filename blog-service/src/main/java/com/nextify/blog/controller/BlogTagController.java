package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.dto.BlogTagAddRequest; // 导入新增标签请求DTO
import com.nextify.blog.dto.BlogTagUpdateRequest; // 导入更新标签请求DTO
import com.nextify.blog.dto.TagQueryRequest;
import com.nextify.blog.entity.BlogTag; // 导入BlogTag实体
import com.nextify.blog.service.BlogArticleTagService;
import com.nextify.blog.service.BlogTagService;
import com.nextify.blog.vo.BlogTagVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid; // 导入Valid注解
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
     * 分页获取标签列表 (现有功能，返回VO)
     * GET /tags/page
     */
    @PublicApi
    @GetMapping("/page")
    public Result<Page<BlogTagVo>> getTags(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "20") long pageSize){

        return  Result.success(tagService.geAllTags(pageNum, pageSize));
    }

    /**
     * 根据ID列表获取标签信息 (现有功能，返回VO)
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
     * 根据ID获取标签详情 (新增功能，返回实体)
     * GET /tags/{id}
     */
    @PublicApi
    @GetMapping("/{id}")
    public Result<BlogTag> getTagById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    /**
     * 新增标签 (新增功能)
     * POST /tags
     */
    @PostMapping
    public Result<Long> addTag(@Valid @RequestBody BlogTagAddRequest request) {
        return Result.success(tagService.addTag(request));
    }

    /**
     * 更新标签 (新增功能)
     * PUT /tags/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @Valid @RequestBody BlogTagUpdateRequest request) {
        tagService.updateTag(id, request);
        return Result.success();
    }

    /**
     * 删除标签 (新增功能)
     * DELETE /tags/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
