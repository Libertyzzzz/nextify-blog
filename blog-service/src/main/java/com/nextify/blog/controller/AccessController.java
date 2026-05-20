package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RateLimiter;
import com.nextify.blog.dto.AccessCodeAddRequest;
import com.nextify.blog.entity.AccessCode;
import com.nextify.blog.service.AccessCodeService;
import com.nextify.blog.vo.AccessCodeVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/access-code")
public class AccessController {
    /**
     * 网站门禁验证
     */
    @Resource
    private AccessCodeService accessCodeService;


    @GetMapping("/verify")
    public Result<Boolean>  getAccessCode(@RequestParam Integer id,
                                          @RequestParam String accessCode){
        return Result.success(accessCodeService.verify(id, accessCode));
    }

    @PostMapping()
    public Result<Integer> setAccessCode(@RequestBody AccessCodeAddRequest request){
        return Result.success(accessCodeService.save(request));
    }
    @RateLimiter(time = 5, count = 1, message = "请勿重复点击")
    @GetMapping("/rate-limit")
    public Result<String> rataLimitTest(){
        return Result.success("正常访问");
    }
}
