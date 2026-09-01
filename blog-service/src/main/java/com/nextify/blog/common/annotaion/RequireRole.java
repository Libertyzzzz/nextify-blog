package com.nextify.blog.common.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解
 * 用于标记需要特定角色的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 需要的角色编码数组
     */
    String[] value();

    /**
     * 角色校验逻辑
     * AND: 必须拥有所有指定角色 (默认)
     * OR:  只需拥有任一角色
     */
    Logic logic() default Logic.OR;

    enum Logic {
        AND,
        OR
    }
}
