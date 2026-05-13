package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.service.AssessmentService;
import com.nextify.blog.vo.AssessmentVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/assessment")
@Slf4j
public class AssessmentController {

    @Resource
    private AssessmentService assessmentService;

    /**
     * 生成评估数据 并进行保存
     * 生成唯一的 分享ID
     */

    @PostMapping("/evaluate")
    public Result<AssessmentVO> evaluate(@RequestBody AssessmentRequestDTO dto,
                                         @RequestParam String gender) {
        if(StringUtils.isEmpty(dto) || gender.isEmpty())
            return Result.fail("请输入正确的信息");
        log.info("gender==" + gender);
        return Result.success(assessmentService.evaluate(dto, gender));
    }

    /**
     * 根据shareId 查询分享快照
     * @param shareId
     */
    @GetMapping("/share/{shareId}")
    public Result<AssessmentVO> getShareInfos(@PathVariable String shareId){
        AssessmentVO res =  assessmentService.findByShareId(shareId);
        if(ObjectUtils.isEmpty(res))
            return Result.fail("查询记录为空");
        return Result.success(res);
    }


}




