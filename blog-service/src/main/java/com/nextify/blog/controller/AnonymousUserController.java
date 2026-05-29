package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.entity.AnonymousUser;
import com.nextify.blog.mapper.AnonymousUserMapper;
import com.nextify.blog.service.AnonymousUserService;
import jakarta.annotation.Resource;
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

//    @GetMapping("/list")
//    public Page<AnonymousUser> list(@RequestParam(defaultValue = "1") long current,
//                                   @RequestParam(defaultValue = "10") long size) {
//      return null;
//    }
//
//    @PostMapping("/user")
//    public Result<Void> saveUser() {
//        anonymousUserService.saveAnonymousUser();
//        return Result.success();
//    }
}