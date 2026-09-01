package com.nextify.blog.common.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于标记需要权限校验的方法
 * 支持 AND/OR 逻辑组合
 *
 * 示例:
 *   @RequirePermission("article:create")                           // 必须拥有该权限
 *   @RequirePermission({"article:create", "article:edit"})        // AND: 必须拥有所有权限
 *   @RequirePermission(value = {"article:create", "article:edit"}, logic = Logic.OR) // OR: 任一权限即可
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限编码数组
     */
    String[] value();

    /**
     * 权限校验逻辑
     * AND: 必须拥有所有指定权限 (默认)
     * OR:  只需拥有任一权限
     */
    Logic logic() default Logic.AND;

    enum Logic {
        AND,
        OR
    }
}
