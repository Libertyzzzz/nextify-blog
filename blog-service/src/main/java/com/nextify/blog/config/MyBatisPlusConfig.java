package com.nextify.blog.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // 导入 Configuration 注解

/**
 * MyBatis-Plus 配置类
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * 主要用于添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        // PaginationInnerInterceptor 是 MyBatis-Plus 提供的分页拦截器
        // DbType.MYSQL 表示数据库类型是 MySQL，MyBatis-Plus 会根据此生成对应的分页 SQL
        // 如果使用的是其他数据库，请修改为对应的 DbType，例如 DbType.POSTGRE_SQL, DbType.ORACLE 等
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
