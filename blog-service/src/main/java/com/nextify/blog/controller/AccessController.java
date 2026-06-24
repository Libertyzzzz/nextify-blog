package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.common.annotaion.RateLimiter;
import com.nextify.blog.common.third.AMapComponent;
import com.nextify.blog.dto.AccessCodeAddRequest;
import com.nextify.blog.entity.AccessCode;
import com.nextify.blog.service.AccessCodeService;
import com.nextify.blog.vo.AccessCodeVo;
import com.nextify.blog.vo.AddressInfoVO;
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

    @Resource
    private AMapComponent aMapComponent;
    @PublicApi
    @GetMapping("/verify")
    public Result<Boolean>  getAccessCode(@RequestParam Integer id,
                                          @RequestParam String accessCode){
        return Result.success(accessCodeService.verify(id, accessCode));
    }

    @PublicApi
    @GetMapping("/{id}")
    public Result<AccessCodeVo> getAccessCode(@PathVariable Integer id){
        return Result.success(accessCodeService.getAccessCodeVo(id));
    }

    @PublicApi
    @PostMapping()
    public Result<Integer> setAccessCode(@RequestBody AccessCodeAddRequest request){
        return Result.success(accessCodeService.save(request));
    }

    @PublicApi
    @RateLimiter(time = 5, count = 1, message = "请勿重复点击")
    @GetMapping("/rate-limit")
    public Result<String> rataLimitTest(){
        return Result.success("正常访问");
    }


    @PublicApi
    @GetMapping("/address")
    public Result<String> getAddressInfo(@RequestParam String longitude, String latitude){

        return Result.success(aMapComponent.getDetailAddress(longitude, latitude));
    }
}
