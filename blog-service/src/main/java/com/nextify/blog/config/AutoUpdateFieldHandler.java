package com.nextify.blog.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AutoUpdateFieldHandler implements MetaObjectHandler {
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "expiredTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "expiredTime", LocalDateTime.class, LocalDateTime.now());


    }
}
