package com.nextify.blog.service.impl;

import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.enums.FemaleAssessmentEnum;
import com.nextify.blog.enums.MaleAssessmentEnum;
import com.nextify.blog.service.AssessmentService;
import com.nextify.blog.vo.AssessmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AssessmentServiceImpl implements AssessmentService {

    @Override
    public AssessmentVO evaluate(AssessmentRequestDTO dto, String gender) {
        log.info("开始人间估值计算，目标性别: {}, 原始年薪: {}", gender, dto.getAnnualIncome());

        // 1. 基础矩阵计算：将 32 个变量全部压入 6 个核心维度
        BaseMatrix matrix = calculateBaseMatrix(dto);

        // 2. 计算说谎因子 (0.0 - 0.85)
        double lie = calculateLieFactor(dto);

        double finalScore;
        String marketLabel;
        String reportContent;

        // 3. 性别分治二次计算逻辑
        if ("MALE".equals(gender)) {
            // 男性：侧重资产与资源 (45% + 15%)
            double raw = matrix.assets * 0.40
                    + matrix.social * 0.10
                    + matrix.biological * 0.25
                    + matrix.aesthetic * 0.10
                    + matrix.emotional * 0.10
                    + matrix.maintenance * 0.05;

            // 性格惩罚税：冷暴力或极端任性直接打 8 折
            double tax = (dto.getStubbornness() + dto.getColdViolenceProb() > 12) ? 0.8 : 1.0;
            finalScore = applyChaos(raw * tax * Math.pow(1 - lie, 2.0));

            // 映射到男性特定的毒舌 Enum
            MaleAssessmentEnum res = MaleAssessmentEnum.getResult(
                    finalScore, dto.getStubbornness(), dto.getAnnualIncome(), dto.getAestheticStyle()
            );
            marketLabel = res.getLabel();
            reportContent = res.getContent();
        } else {
            // 女性：侧重生物资产与审美 (40% + 20%)
            double raw = matrix.biological * 0.45
                    + matrix.aesthetic * 0.10
                    + matrix.assets * 0.15
                    + matrix.emotional * 0.15
                    + matrix.social * 0.10
                    + matrix.maintenance * 0.05;

            // 资产杠杆：高收入可以对冲部分任性带来的扣分
            double taxRate = (matrix.assets > 70) ? 0.03 : 0.07;
            double tax = 1.0 - Math.max(0, (dto.getStubbornness() - 6)) * taxRate;

            finalScore = applyChaos(raw * tax * Math.pow(1 - lie, 1.5));

            // 映射到女性特定的毒舌 Enum
            double bmi = dto.getWeight() / Math.pow(dto.getHeight() / 100.0, 2);
            FemaleAssessmentEnum res = FemaleAssessmentEnum.getResult(
                    finalScore, dto.getAestheticStyle(), bmi, dto.getStubbornness(), dto.getAnnualIncome()
            );
            marketLabel = res.getLabel();
            reportContent = res.getContent();
        }

        // 4. 封装 VO 结果
        return AssessmentVO.builder()
                .score((int) Math.round(finalScore))
                .marketLevel(marketLabel)
                .report(reportContent)
                .lieFactor(lie)
                .radar(new AssessmentVO.RadarData(
                        (int) Math.min(100, matrix.assets),
                        (int) Math.min(100, matrix.biological),
                        (int) Math.min(100, matrix.aesthetic),
                        (int) Math.min(100, matrix.emotional),
                        (int) Math.min(100, (matrix.social + (1 - lie) * 10)), // 诚实度加持社交分
                        (int) Math.min(100, matrix.maintenance)
                ))
                .build();
    }

    private BaseMatrix calculateBaseMatrix(AssessmentRequestDTO dto) {
        BaseMatrix m = new BaseMatrix();

        // 1. 生存资源 (Assets) - 6个变量
        m.assets = (Math.log10(dto.getAnnualIncome() / 100.0 / 10000.0 + 1) / Math.log10(101) * 55)
                + (dto.getHouseStatus() * 15) + (dto.getCarLevel() * 10)
                + (dto.getWorkStability() * 5) + (dto.getParentPension() * 5)
                + (dto.getFamilyStructure() == 1 ? 10 : 5);

        // 2. 生物属性 (Biological) - 6个变量
        double bmi = dto.getWeight() / Math.pow(dto.getHeight() / 100.0, 2);
        m.biological = (dto.getHeight() - 150) * 1.5
                + (Math.max(0, 100 - Math.abs(bmi - 21) * 8))
                + (dto.getHairStatus() * 10) + (dto.getEyeStatus() * 5)
                - (dto.getGeneticRisk() ? 20 : 0);

        // 3. 审美溢价 (Aesthetic) - 5个变量
        m.aesthetic = (dto.getAestheticStyle() * 10) + (dto.getPhotoSkill() * 8)
                + (dto.getFashionInvestment() * 10) + (dto.getTravelPlanning() * 5)
                + (dto.getSocialFilters() * 3);

        // 4. 情绪带宽 (Emotional) - 5个变量
        m.emotional = (dto.getEmpathyLevel() * 10) + (dto.getEmotionalStability() * 12)
                + (dto.getSharingDesire() * 8) + (dto.getPetType() * 5)
                + (dto.getRelationshipGoal() == 1 ? 10 : 5);

        // 5. 社交博弈 (Social) - 5个变量
        m.social = (dto.getTalkBreadth() * 12) + (dto.getExtraSkills() * 8)
                + (dto.getCookingSkill() * 6) + (dto.getHouseworkLevel() * 6)
                + (20 - dto.getReplyLatency() * 4);

        // 6. 维护成本 (Maintenance) - 5个变量 (分高 = 维护成本低)
        m.maintenance = (10 - dto.getStubbornness()) * 4
                + (5 - dto.getExBonding()) * 8
                + (5 - dto.getColdViolenceProb()) * 8
                + (10 - dto.getControlDesire() * 2)
                + (dto.getConsumptionView() == 2 ? 10 : 5); // 消费观匹配

        return m;
    }

    private double calculateLieFactor(AssessmentRequestDTO dto) {
        double l = 0.0;
        // 逻辑冲突判定
        if (dto.getVisualHeight() - dto.getHeight() > 6) l += 0.3; // 身高注水严重
        if (dto.getSocialFilters() > 4 && dto.getPhotoSkill() < 2) l += 0.2; // 审美与技术不符
        if (dto.getAnnualIncome() > 0 && dto.getAnnualIncome() % 1000000 == 0) l += 0.1; // 收入过于整齐
        if (dto.getFashionInvestment() > 40 && dto.getAnnualIncome() < 10000000) l += 0.15; // 精致穷校验
        return Math.min(l, 0.85);
    }

    private double applyChaos(double score) {
        // 引入 ±2% 的随机波动，增加系统的真实感
        return Math.max(0, Math.min(100, score * (0.98 + Math.random() * 0.04)));
    }

    private static class BaseMatrix {
        double assets, biological, aesthetic, emotional, social, maintenance;
    }
}