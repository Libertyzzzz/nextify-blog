package com.nextify.blog;


import com.nextify.blog.utils.SensitiveWordFilter;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = BlogApplication.class)
class DataImportTest {

    @Resource
    private SensitiveWordFilter sensitiveWordFilter;

//    @Test
//    void testSensitiveWordImport(){
//        sensitiveWordFilter.loadSensitiveWordsFromResources();
//    }

}
