package com.nextify.blog.dto;

import lombok.Data;

@Data
public class AssessmentRequestDTO {
    // 1. 底层资产与生物属性 (12个)
    private String gender;
    private Integer age;
    private String imageUrl;
    private String longitude;
    private String latitude;
    private Integer height;           // 净身高
    private Integer visualHeight;     // 视觉身高
    private Double weight;            // 体重
    private Integer hairStatus;       // 发量 (0:秃 1:稀 2:平 3:茂)
    private Integer eyeStatus;        // 视力/美化习惯 (0:框架 1:素颜 2:美瞳)
    private Long annualIncome;        // 年薪 (分)
    private Integer workStability;    // 职业稳定性 (1-5)
    private Integer houseStatus;      // 房产 (0:租 1:有房有贷 2:红本在手)
    private Integer carLevel;         // 车辆 (0-3)
    private Integer parentPension;    // 父母养老 (0-3)
    private Integer familyStructure;  // 家庭结构 (0:多子 1:独生)
    private Boolean geneticRisk;      // 遗传风险

    // 2. 软实力与生活品味 (10个)
    private Integer aestheticStyle;   // 审美风格 (1-5)
    private Integer photoSkill;       // 摄影水平 (1-5)
    private Integer cookingSkill;     // 厨艺 (1-4)
    private Integer travelPlanning;   // 旅行规划 (1-3)
    private Integer houseworkLevel;   // 家务参与 (1-4)
    private Integer extraSkills;      // 技能特长数量
    private Integer petType;          // 宠物类型
    private Integer consumptionView;  // 消费观 (1-4)
    private Double fashionInvestment; // 穿搭预算占比
    private Integer talkBreadth;   // 知识面/接梗能力 (1-5)

    // 3. 心理架构与社交博弈 (10个)
    private Integer stubbornness;     // 任性值 (1-10)
    private Integer emotionalStability;// 情绪稳态 (1-5)
    private Integer replyLatency;     // 回消延迟 (1:秒回 4:轮回)
    private Integer exBonding;        // 前任纠缠度 (1-5)
    private Integer socialFilters;    // 朋友圈美化率 (1-5)
    private Integer controlDesire;    // 控制欲 (1-5)
    private Integer sharingDesire;    // 分享欲 (1-5)
    private Integer relationshipGoal; // 恋爱目的 (1:纯爱 4:结婚)
    private Integer coldViolenceProb; // 冷暴力倾向 (1-5)
    private Integer empathyLevel;     // 同理心 (1-5)
}
