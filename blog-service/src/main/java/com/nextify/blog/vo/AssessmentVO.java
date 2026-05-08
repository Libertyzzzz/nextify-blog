package com.nextify.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AssessmentVO {
    private Integer score;            // 最终脱水得分
    private Double lieFactor;         // 说谎因子 (0.0-1.0)
    private String marketLevel;       // 市场分段标签
    private String report;           // 毒舌点评报告

    // 雷达图数据结构
    private RadarData radar;

    @Data
    @AllArgsConstructor
    public static class RadarData {
        private Integer assets;      // 生存资源
        private Integer biological;  // 生物属性
        private Integer aesthetic;   // 审美溢价
        private Integer emotional;   // 情绪带宽
        private Integer social;      // 社交博弈
        private Integer maintenance; // 维护成本 (分数越高代表成本越低，即越省心)
    }
}