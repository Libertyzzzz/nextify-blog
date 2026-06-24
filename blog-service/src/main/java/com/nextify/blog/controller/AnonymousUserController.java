package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.entity.AnonymousUser;
import com.nextify.blog.mapper.AnonymousUserMapper;
import com.nextify.blog.service.AnonymousUserService;
import com.nextify.blog.utils.IPUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 匿名用户画像查询接口（自用管理）
 */
@RestController
@RequestMapping("/anonymous")
public class AnonymousUserController {

    @Resource
    private AnonymousUserService anonymousUserService;


    @PublicApi
    @RequestMapping("/user")
    public Result<AnonymousUser> getIdentify(HttpServletRequest request){
        String ip = IPUtils.getRealIp(request);
        String userAgent = request.getHeader("User-Agent");
        return Result.success(anonymousUserService.getIdentity(ip, userAgent));
    }

}