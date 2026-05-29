package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.entity.AnonymousUser;

public interface AnonymousUserService extends IService<AnonymousUser> {
    void saveAnonymousUser();

    void processAnonymousUser(String ip, String userAgent);

}
