package com.nextify.blog.service;

public interface QuotaService {
    /**
     * 检查用户配额是否足够
     * @param userId 用户ID
     * @param tokensNeeded 需要消耗的token数量
     * @return true如果配额足够，否则false
     */
    boolean checkAndConsumeQuota(Long userId, Integer tokensNeeded);
}
