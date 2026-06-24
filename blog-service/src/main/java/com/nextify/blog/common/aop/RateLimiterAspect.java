package com.nextify.blog.common.aop;

import com.nextify.blog.common.annotaion.RateLimiter;
import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.utils.IPUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Aspect
public class RateLimiterAspect {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private HttpServletRequest servletRequest;

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws  Throwable{
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        String ip = IPUtils.getRealIp(servletRequest);
        String methodName = point.getSignature().getName();

        // 检查黑名单。判断是否已封禁
        String blackListKey = "limit:blacklist:" + ip;
        if(redisTemplate.hasKey(blackListKey))
            throw new BusinessException("检测到恶意行为，你的IP已被封禁");
        long now = System.currentTimeMillis();
        // 滑动窗口的 Key
        String redisKey = "limit:window:" + methodName + ":" + ip;
        // 阈值统计的 Key（用来算一分钟内总违规次数，防止盗刷）
        String totalCountKey = "limit:total:" + ip;

        // 窗口移动
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, (now - time * 1000L));

        // 获取该ip当前窗口内的请求总数
        Long currCount = redisTemplate.opsForZSet().zCard(redisKey);
        if(currCount != null && currCount >= count){
            // 触发限流
            Long totalViolations = redisTemplate.opsForValue().increment(totalCountKey);
            // 第一次违反 冻结一分钟
            if(totalViolations != null && totalViolations == 1){
                redisTemplate.expire(totalCountKey, 1, TimeUnit.MINUTES);
            }
            // 超过30次。直接加黑
            if(totalViolations != null && totalViolations > 30){
                redisTemplate.opsForValue().set(blackListKey, "1", 1, TimeUnit.HOURS);
            }
            // 如果触发限流 且没有加黑 则直接给出提醒
            throw new BusinessException(rateLimiter.message());
        }

        // 初次访问接口 直接放行
        redisTemplate.opsForZSet().add(redisKey, String.valueOf(now), now);
        redisTemplate.expire(redisKey, time + 2, TimeUnit.SECONDS);

    }

}
