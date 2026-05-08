package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.service.AssessmentService;
import com.nextify.blog.vo.AssessmentVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v2/assessment")
@Slf4j
public class AssessmentController {

    @Resource
    private AssessmentService assessmentService;
    @PostMapping("/evaluate")
    public Result<AssessmentVO> evaluate(@RequestBody AssessmentRequestDTO dto,
                                         @RequestParam String gender) {
        if(StringUtils.isEmpty(dto) || gender.isEmpty())
            return Result.fail("请输入正确的信息");
        log.info("gender==" + gender);
        return Result.success(assessmentService.evaluate(dto, gender));
    }








}