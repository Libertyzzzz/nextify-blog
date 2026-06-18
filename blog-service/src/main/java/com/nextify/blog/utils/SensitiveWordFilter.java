package com.nextify.blog.utils;

import cn.hutool.dfa.SensitiveUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class SensitiveWordFilter {


    @PostConstruct
    public void init(){
        log.info("Initializing sensitive word filter....");
        Set<String> sensitiveWords = new HashSet<>();

        sensitiveWords.add("敏感词");
        sensitiveWords.add("脏话");
        sensitiveWords.add("违法");
        sensitiveWords.add("色情");
        sensitiveWords.add("广告");
        sensitiveWords.add("傻逼");
        sensitiveWords.add("卧槽");
        sensitiveWords.add("你妈的");
        sensitiveWords.add("操你");
        sensitiveWords.add("小许");
        sensitiveWords.add("许贤斌");
        sensitiveWords.add("爸爸");
        SensitiveUtil.init(sensitiveWords);
        log.info("Sensitive words load completed: {}", sensitiveWords.size());
    }

    public boolean containsSensitiveWord(String text) {
    return SensitiveUtil.containsSensitive(text);
    }

    /**
     * 替换文本中的敏感词，默认使用 '*' 替换
     * @param text 待替换文本
     * @return 替换后的文本
     */
    public String replaceSensitiveWord(String text) {
        return SensitiveUtil.sensitiveFilter(text);
    }



}
