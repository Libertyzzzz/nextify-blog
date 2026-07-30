package com.nextify.blog.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
    private Snowflake snowflake;


    @PostConstruct
    public void init(){
        this.snowflake = IdUtil.getSnowflake(1, 1);
    }

    public long nextId() {
        return snowflake.nextId();
    }

    public String nextIdStr(){
        return snowflake.nextIdStr();
    }



}
