package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.entity.AnonymousUser;
import com.nextify.blog.mapper.AnonymousUserMapper;
import com.nextify.blog.service.AnonymousUserService;
import com.nextify.blog.utils.IPUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AnonymousUserServiceImpl extends ServiceImpl<AnonymousUserMapper, AnonymousUser> implements AnonymousUserService {

    @Autowired
    private AnonymousUserMapper anonymousUserMapper;

    @Resource
    private HttpServletRequest servletRequest;

    @Override
    public void saveAnonymousUser() {
        String ip = IPUtils.getRealIp(servletRequest);
        processAnonymousUser(ip, servletRequest.getHeader("User-Agent"));
    }
    @Async("taskExecutor")
    @Override
    public void processAnonymousUser(String ip, String userAgent) {
        // 1. 生成设备指纹哈希
        String rawFingerprint = ip + "|" + userAgent;
        // log.info("设备指纹：{}", rawFingerprint);
        String fingerprint = DigestUtils.md5DigestAsHex(rawFingerprint.getBytes());

        // 2. 查找是否存在该匿名用户
        AnonymousUser user = anonymousUserMapper.selectOne(
                new LambdaQueryWrapper<AnonymousUser>().eq(AnonymousUser::getFingerprint, fingerprint)
        );

        if (user == null) {
            // 3. 新匿名用户：创建画像
            user = new AnonymousUser();
            user.setAnonymousId(UUID.randomUUID().toString().replace("-", ""));
            user.setFingerprint(fingerprint);
            user.setRawFingerprint(rawFingerprint);
            user.setIpAddress(ip);
            user.setVisitCount(1);
            user.setBrowser(parseBrowser(userAgent));
            user.setOs(parseOs(userAgent));
            anonymousUserMapper.insert(user);
        } else {
            // 4. 老用户：累加计数，更新最后访问时间（MyBatis Plus 自动更新逻辑或手动设置）
            user.setVisitCount(user.getVisitCount() + 1);
            user.setIpAddress(ip);
            anonymousUserMapper.updateById(user);
        }
    }

    @Override
    public String findByFingerprint(String fingerprint) {
         AnonymousUser user =  anonymousUserMapper.selectOne(new LambdaQueryWrapper<AnonymousUser>()
            .eq(AnonymousUser::getFingerprint, fingerprint));

         return user.getAnonymousId();
    }

    @Override
    public AnonymousUser getIdentity(String ip, String userAgent) {
        String rawFingerprint = ip + "|" + userAgent;
        // log.info("设备指纹：{}", rawFingerprint);
        String fingerprint = DigestUtils.md5DigestAsHex(rawFingerprint.getBytes());

        // 2. 查找是否存在该匿名用户
        AnonymousUser user = anonymousUserMapper.selectOne(
            new LambdaQueryWrapper<AnonymousUser>()
                .eq(AnonymousUser::getFingerprint, fingerprint)
                .select(AnonymousUser::getAnonymousId, AnonymousUser::getHasCommented)
        );

        log.info("查询匿名身份");

        if(user == null){
            processAnonymousUser(ip, userAgent);
        }

        return user;

    }

    @Override
    public Boolean updateStatus(String anonymousId) {
        return this.update(new UpdateWrapper<AnonymousUser>().eq("anonymous_id", anonymousId).set("has_commented", true));
    }

    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Edge")) return "Edge";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if(ua.contains("Safari")) return "Safari";
        return "Other";
    }

    private String parseOs(String ua) {
        if (ua == null) return "Unknown";
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone")) return "iOS";
        return "Other";
    }

    /**
     * 解析主流手机品牌
     */
    private String parseDeviceBrand(String ua) {
        if (ua == null || !ua.contains("Android") && !ua.contains("iPhone") && !ua.contains("iPad")) {
            return "PC/Unknown";
        }

        String uaUpper = ua.toUpperCase();

        // 1. 苹果
        if (uaUpper.contains("IPHONE")) return "Apple (iPhone)";
        if (uaUpper.contains("IPAD")) return "Apple (iPad)";

        // 2. 华为 & 荣耀
        if (uaUpper.contains("HUAWEI") || uaUpper.contains("HONOR")) return "Huawei/Honor";

        // 3. 小米
        if (uaUpper.contains("XIAOMI") || uaUpper.contains("MI ") || uaUpper.contains("REDMI") || uaUpper.contains("MIX ")) return "Xiaomi";

        // 4. 三星
        if (uaUpper.contains("SAMSUNG") || uaUpper.contains("SM-")) return "Samsung";

        // 5. OPPO & vivo
        if (uaUpper.contains("OPPO") || uaUpper.contains("PCLM00") || uaUpper.contains("PACM00")) return "OPPO";
        if (uaUpper.contains("VIVO")) return "vivo";

        return "Android Generic";
    }

}