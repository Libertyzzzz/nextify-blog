package com.nextify.blog.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AssessmentRecordDTO {
    private String gender;
    private Integer score;
    private String marketLevel;
    private String report;
    private Map<String, Integer> radar; // 六维雷达数据
    private Map<String, Object> inputs; // 32个输入变量
}
