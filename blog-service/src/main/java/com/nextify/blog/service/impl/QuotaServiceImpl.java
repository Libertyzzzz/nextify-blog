package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextify.blog.entity.AgentQuota;
import com.nextify.blog.mapper.AgentQuotaMapper;
import com.nextify.blog.service.QuotaService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class QuotaServiceImpl implements QuotaService {

    @Resource
    private AgentQuotaMapper agentQuotaMapper;

    @Override
    @Transactional
    public boolean checkAndConsumeQuota(Long userId, Integer tokensNeeded) {
        // 获取当前月份的配额
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        AgentQuota quota = agentQuotaMapper.selectOne(new QueryWrapper<AgentQuota>()
                .eq("user_id", userId)
                .eq("period", "monthly") // 暂时只考虑月度配额
                .between("period_end_at", monthStart, monthEnd) // 查找当前月份的配额
        );

        if (quota == null) {
            // 如果没有找到配额，可以根据业务逻辑创建默认配额或者直接返回不足
            // 这里我们假设默认用户有100000 tokens的月度配额
            quota = new AgentQuota();
            quota.setUserId(userId);
            quota.setPeriod("monthly");
            quota.setQuotaTokens(100000L); // 默认10万tokens
            quota.setUsedTokens(0L);
            quota.setPeriodEndAt(monthEnd);
            quota.setCreatedAt(now);
            quota.setUpdatedAt(now);
            agentQuotaMapper.insert(quota);
        }

        if (quota.getUsedTokens() + tokensNeeded > quota.getQuotaTokens()) {
            return false; // 配额不足
        }

        // 消耗配额
        quota.setUsedTokens(quota.getUsedTokens() + tokensNeeded);
        quota.setUpdatedAt(now);
        agentQuotaMapper.updateById(quota);

        return true;
    }
}
