package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.dto.TagQueryRequest;
import com.nextify.blog.entity.BlogArticleTag;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.service.BlogArticleTagService;
import com.nextify.blog.service.BlogTagService;
import com.nextify.blog.vo.BlogTagVo;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class BlogTagController {

    @Resource
    private BlogArticleTagService articleTagService;
    @Resource
    private BlogTagService tagService;

    @GetMapping("/page")
    public Result<Page<BlogTagVo>> getTags(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize){

        return  Result.success(tagService.geAllTags(pageNum, pageSize));
    }
    @GetMapping()
    public Result<List<BlogTagVo>> getTagInfos(TagQueryRequest request) {
        if(request == null)
            return Result.fail("未传入任何参数");
        return Result.success(tagService.getTagInfos(request.getIds()));
    }

}
