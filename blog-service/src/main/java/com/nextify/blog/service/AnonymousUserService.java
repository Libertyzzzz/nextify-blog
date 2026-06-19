package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.entity.AnonymousUser;

import java.util.Map;

public interface AnonymousUserService extends IService<AnonymousUser> {
    void saveAnonymousUser();

    void processAnonymousUser(String ip, String userAgent);

    String findByFingerprint(String fingerprint);

    AnonymousUser getIdentity(String ip, String userAgent);

    Boolean updateStatus(String anonymousId);

}
