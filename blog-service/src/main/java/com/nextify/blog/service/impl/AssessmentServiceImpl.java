package com.nextify.blog.service.impl;

import com.alibaba.fastjson2.JSON;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.entity.AssessmentRecord;
import com.nextify.blog.enums.FemaleAssessmentEnum;
import com.nextify.blog.enums.MaleAssessmentEnum;
import com.nextify.blog.mapper.AssessmentRecordMapper;
import com.nextify.blog.service.AssessmentService;
import com.nextify.blog.vo.AssessmentVO;
import io.netty.util.internal.ObjectUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class AssessmentServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord> implements AssessmentService {

    private static final String SEED = "23456789abcdefghjkmnpqrstuvwxyz";
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private AssessmentRecordMapper assessmentMapper;

    @Override
    public AssessmentVO evaluate(AssessmentRequestDTO dto, String gender) {
        log.info("开始人间估值计算，目标性别: {}, 原始年薪: {}", gender, dto.getAnnualIncome());

        // 1. 基础矩阵计算：将 32 个变量全部压入 6 个核心维度
        BaseMatrix matrix = calculateBaseMatrix(dto);

        // 2. 计算说谎因子 (0.0 - 0.85)
        float lie = calculateLieFactor(dto);

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

        // 生成shareId
        String shareId = NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                SEED.toCharArray(),
                8

        );
        // 5. 封装 VO 结果
        AssessmentVO res = AssessmentVO.builder()
                .score((int) Math.round(finalScore))
                .gender(gender)
                .marketLevel(marketLabel)
                .report(reportContent)
                .lieFactor(lie)
                .shareId(shareId)
                .radar(new AssessmentVO.RadarData(
                        (int) Math.min(100, matrix.assets),
                        (int) Math.min(100, matrix.biological),
                        (int) Math.min(100, matrix.aesthetic),
                        (int) Math.min(100, matrix.emotional),
                        (int) Math.min(100, (matrix.social + (1 - lie) * 10)), // 诚实度加持社交分
                        (int) Math.min(100, matrix.maintenance)
                ))
                .build();
        // 5. 保存记录
        saveRecord(res, dto);

        return res;
    }

    @Override
    public AssessmentVO findByShareId(String shareId) {
        LambdaQueryWrapper<AssessmentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssessmentRecord::getShareId, shareId);
        AssessmentRecord record = assessmentMapper.selectOne(queryWrapper);
        if(ObjectUtils.isEmpty(record)){
            log.info("该分享记录已经被抹除");
            return null;
        }


        AssessmentVO.RadarData radarData = JSON.parseObject(record.getRadarData(), AssessmentVO.RadarData.class);
        return AssessmentVO.builder()
                .gender(record.getGender())
                .score(record.getScore())
                .lieFactor(record.getLieFactor())
                .marketLevel(record.getMarketLevel())
                .report(record.getReport())
                .radar(radarData)
                .build();
    }

    private void saveRecord(AssessmentVO assessment, AssessmentRequestDTO dto) {
        AssessmentRecord record = new AssessmentRecord();
        record.setShareId(assessment.getShareId());
        record.setScore(assessment.getScore());
        record.setGender(assessment.getGender());
        record.setReport(assessment.getReport());
        record.setRadarData(JSON.toJSONString(assessment.getRadar()));
        record.setRawInput(JSON.toJSONString(dto));
        record.setLieFactor(assessment.getLieFactor());
        record.setMarketLevel(assessment.getMarketLevel());

        assessmentMapper.insert(record);

   }




    private BaseMatrix calculateBaseMatrix(AssessmentRequestDTO dto) {
        BaseMatrix m = new BaseMatrix();

        // --- 1. 生存资源 (Assets) - 调整基数，引入对数压缩 ---
        double rawAssets = (Math.log10(dto.getAnnualIncome() / 100.0 / 10000.0 + 1) / Math.log10(101) * 60) // 提高收入权重
                + (dto.getHouseStatus() * 12)
                + (dto.getCarLevel() * 8)
                + (dto.getWorkStability() * 4)
                + (dto.getParentPension() * 4)
                + (dto.getFamilyStructure() == 1 ? 6 : 3);
        m.assets = softenScore(rawAssets, 140); // 140是预估的优秀值，超过它会极难增长

        // --- 2. 生物属性 (Biological) - 核心是BMI和身高 ---
        double bmi = dto.getWeight() / Math.pow(dto.getHeight() / 100.0, 2);
        double bmiScore = Math.max(0, 100 - Math.abs(bmi - 21.5) * 12); // BMI更加严苛
        double heightScore = (dto.getGender().equals("MALE") ? (dto.getHeight() - 160) : (dto.getHeight() - 150)) * 2;

        double rawBio = (bmiScore * 0.5) + (heightScore * 0.3) + (dto.getHairStatus() * 8) + (dto.getEyeStatus() * 4);
        m.biological = softenScore(rawBio, 120);

        // --- 3. 审美溢价 (Aesthetic) ---
        double rawAes = (dto.getAestheticStyle() * 8) + (dto.getPhotoSkill() * 6)
                + (dto.getFashionInvestment() * 6) + (dto.getTravelPlanning() * 4)
                + (dto.getSocialFilters() * 2);
        m.aesthetic = (rawAes / 120.0) * 100; // 线性缩放到100以内

        // --- 4. 情绪带宽 (Emotional) ---
        double rawEmo = (dto.getEmpathyLevel() * 8) + (dto.getEmotionalStability() * 10)
                + (dto.getSharingDesire() * 6) + (dto.getPetType() * 4)
                + (dto.getRelationshipGoal() == 1 ? 8 : 4);
        m.emotional = (rawEmo / 130.0) * 100;

        // --- 5. 社交博弈 (Social) ---
        double rawSoc = (dto.getTalkBreadth() * 10) + (dto.getExtraSkills() * 6)
                + (dto.getCookingSkill() * 5) + (dto.getHouseworkLevel() * 5)
                + (20 - dto.getReplyLatency() * 4);
        m.social = (rawSoc / 120.0) * 100;

        // --- 6. 维护成本 (Maintenance) ---
        double rawMain = (10 - dto.getStubbornness()) * 4
                + (5 - dto.getExBonding()) * 6
                + (5 - dto.getColdViolenceProb() * 1.5) * 6
                + (10 - dto.getControlDesire() * 2)
                + (dto.getConsumptionView() == 2 ? 8 : 4);
        m.maintenance = (rawMain / 100.0) * 100;

        return m;
    }

    /**
     * 软性封顶函数：平滑高分段
     * 逻辑：如果原始分超过 80，后续的增长将变得缓慢
     */
    private double softenScore(double raw, double targetMax) {
        double normalized = (raw / targetMax) * 100;
        if (normalized <= 85) return normalized;
        // 超过85分后，每增加10分，实际只反映为2分，极难达到100
        return 85 + (normalized - 85) * 0.2;
    }

    private float calculateLieFactor(AssessmentRequestDTO dto) {
        float l = 0.0f;
        // 逻辑冲突判定
        if (dto.getVisualHeight() - dto.getHeight() > 6) l += 0.3f;// 身高注水严重
        if (dto.getSocialFilters() > 4 && dto.getPhotoSkill() < 2) l += 0.2f; // 审美与技术不符
        if (dto.getAnnualIncome() > 0 && dto.getAnnualIncome() % 1000000 == 0) l += 0.1f; // 收入过于整齐
        if (dto.getFashionInvestment() > 40 && dto.getAnnualIncome() < 10000000) l += 0.15f; // 精致穷校验
        return Math.min(l, 0.85f);
    }

    private double applyChaos(double score) {
        // 引入 ±2% 的随机波动，增加系统的真实感
        return Math.max(0, Math.min(100, score * (0.98 + Math.random() * 0.04)));
    }

    private static class BaseMatrix {
        double assets, biological, aesthetic, emotional, social, maintenance;
    }
}