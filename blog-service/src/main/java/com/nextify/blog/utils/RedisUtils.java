package com.nextify.blog.utils;


import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public Boolean deleteKey(String key){
        return stringRedisTemplate.delete(key);
    }


    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }



    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public Boolean setExpire(String key, long timeout, TimeUnit unit){
        return stringRedisTemplate.expire(key, timeout, unit);
    }


    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /** 移除过期时间（永久有效） */
    public Boolean persist(String key) {
        return stringRedisTemplate.persist(key);
    }

    // ==================== String 操作 ====================

    /** 设置值 */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /** 设置值并指定过期时间 */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /** 设置值并指定过期时间（秒） */
    public void set(String key, String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /** 获取值 */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /** 获取值并转换为指定类型 */
    public <T> T get(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? JSONUtil.toBean(value, clazz) : null;
    }

    /** 设置对象（序列化为 JSON） */
    public void setObject(String key, Object value) {
        set(key, JSONUtil.toJsonStr(value));
    }

    /** 设置对象并指定过期时间 */
    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        set(key, JSONUtil.toJsonStr(value), timeout, unit);
    }

    /** 获取对象（反序列化 JSON） */
    public <T> T getObject(String key, Class<T> clazz) {
        String value = get(key);
        return value != null ? JSONUtil.toBean(value, clazz) : null;
    }

    /** 递增 */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /** 递增指定步长 */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /** 递减 */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /** 递减指定步长 */
    public Long decrement(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== Hash 操作 ====================

    /** 设置 Hash 字段 */
    public void hSet(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    /** 获取 Hash 字段 */
    public Object hGet(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    /** 获取所有 Hash 字段 */
    public Map<Object, Object> hGetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /** 删除 Hash 字段 */
    public Long hDelete(String key, String... hashKeys) {
        return stringRedisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
    }

    /** 判断 Hash 字段是否存在 */
    public Boolean hHasKey(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().hasKey(key, hashKey);
    }

    // ==================== List 操作 ====================

    /** 左推入 */
    public Long lPush(String key, String value) {
        return stringRedisTemplate.opsForList().leftPush(key, value);
    }

    /** 右推入 */
    public Long rPush(String key, String value) {
        return stringRedisTemplate.opsForList().rightPush(key, value);
    }

    /** 左弹出 */
    public String lPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /** 右弹出 */
    public String rPop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    /** 获取 List 长度 */
    public Long lSize(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    /** 获取 List 范围内的元素 */
    public List<String> lRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /** 获取 List 全部元素 */
    public List<String> lGetAll(String key) {
        return stringRedisTemplate.opsForList().range(key, 0, -1);
    }

    // ==================== Set 操作 ====================

    /** 添加元素到 Set */
    public Long sAdd(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key,  values);
    }

    /** 获取 Set 所有元素 */
    public Set<String> sGetAll(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    /** 判断元素是否在 Set 中 */
    public Boolean sIsMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    /** 获取 Set 大小 */
    public Long sSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    /** 从 Set 移除元素 */
    public Long sRemove(String key, String... values) {
        return stringRedisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    // ==================== Sorted Set 操作 ====================

    /** 添加元素到 ZSet */
    public Boolean zAdd(String key, String value, double score) {
        return stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    /** 按分数范围查询 */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /** 获取 ZSet 排名（从0开始） */
    public Long zRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    /** 获取 ZSet 大小 */
    public Long zSize(String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }


}

