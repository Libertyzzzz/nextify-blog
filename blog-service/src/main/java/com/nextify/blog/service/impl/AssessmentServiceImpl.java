package com.nextify.blog.service.impl;

import com.alibaba.fastjson2.JSON;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextify.blog.common.third.AMapComponent;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.entity.AssessmentRecord;
import com.nextify.blog.enums.FemaleAssessmentEnum;
import com.nextify.blog.enums.MaleAssessmentEnum;
import com.nextify.blog.mapper.AssessmentRecordMapper;
import com.nextify.blog.service.AssessmentService;
import com.nextify.blog.service.GeoLocationService;
import com.nextify.blog.utils.IPUtils;
import com.nextify.blog.vo.AssessmentVO;
import io.netty.util.internal.ObjectUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class AssessmentServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord> implements AssessmentService {

    private static final String SEED = "23456789abcdefghjkmnpqrstuvwxyz";
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private AssessmentRecordMapper assessmentMapper;

    @Resource
    private GeoLocationService geoLocationService;

    @Resource
    private HttpServletRequest servletRequest;

    @Resource
    private AMapComponent aMapComponent;

    @Override
    public AssessmentVO evaluate(AssessmentRequestDTO dto, String gender) {
        log.info("开始人间估值计算，目标性别: {}, 原始年薪: {}", gender, dto.getAnnualIncome());

        // 1. 基础矩阵计算：将 32 个变量全部压入 6 个核心维度
        BaseMatrix matrix = calculateBaseMatrix(dto);

        // 2. 计算说谎因子 (0.0 - 0.85)
        float lie = calculateLieFactor(dto);
        double relationshipScore = calculateRelationshipScore(dto, gender);

        double finalScore;
        String marketLabel;
        String reportContent;

        int age = dto.getAge() == null ? 29 : dto.getAge();
        double incomeContextScore = incomeContextScore(dto.getAnnualIncome(), age, gender);
        double interaction = calculateInteractionBonus(dto, matrix, incomeContextScore);
        double scarcityPremium = calculateScarcityPremium(dto, gender);

        // 3. 性别分治二次计算逻辑
        if ("MALE".equals(gender)) {
            // 男性：侧重资产与资源 (45% + 15%)
            double raw = matrix.assets * 0.33
                    + matrix.social * 0.10
                    + matrix.biological * 0.25
                    + matrix.aesthetic * 0.10
                    + matrix.emotional * 0.05
                    + matrix.maintenance * 0.05
                    + incomeContextScore * 0.07;

            // 男性模型：亲密关系能力作为独立维度，权重更高
            raw += relationshipScore * 0.15;
            raw += interaction + scarcityPremium;

            double penalty = exponentialPenalty(dto);
            double tax = (dto.getStubbornness() + dto.getColdViolenceProb() > 12) ? 0.8 : 1.0;
            double preScore = raw * tax * penalty * Math.pow(1 - lie, 2.0);
            finalScore = applyChaos(zScoreSpread(preScore));

            // 映射到男性特定的毒舌 Enum
            MaleAssessmentEnum res = MaleAssessmentEnum.getResult(
                    finalScore, dto.getStubbornness(), dto.getAnnualIncome(), dto.getAestheticStyle()
            );
            marketLabel = res.getLabel();
            reportContent = res.getContent();
        } else {
            // 女性：侧重生物资产与审美 (40% + 20%)
            double raw = matrix.biological * 0.38
                    + matrix.aesthetic * 0.10
                    + matrix.assets * 0.15
                    + matrix.emotional * 0.05
                    + matrix.social * 0.10
                    + matrix.maintenance * 0.05
                    + incomeContextScore * 0.17;

            // 女性模型：亲密关系能力做独立维度，但强调稳定与边界
            raw += relationshipScore * 0.10;
            raw += interaction + scarcityPremium;

            // 资产杠杆：高收入可以对冲部分任性带来的扣分
            double taxRate = (matrix.assets > 70) ? 0.03 : 0.07;
            double tax = 1.0 - Math.max(0, (dto.getStubbornness() - 6)) * taxRate;
            double penalty = exponentialPenalty(dto);

            double preScore = raw * tax * penalty * Math.pow(1 - lie, 1.5);
            finalScore = applyChaos(zScoreSpread(preScore));

            // 映射到女性特定的毒舌 Enum
            double bmi = dto.getWeight() / Math.pow(dto.getHeight() / 100.0, 2);
            FemaleAssessmentEnum res = FemaleAssessmentEnum.getResult(
                    finalScore, dto.getAestheticStyle(), bmi, dto.getStubbornness(), dto.getAnnualIncome()
            );
            marketLabel = res.getLabel();
            reportContent = res.getContent();
        }

        // 一票否决：遗传风险触发生物安全线截断
        if (Boolean.TRUE.equals(dto.getGeneticRisk())) {
            finalScore = Math.min(finalScore, 58.0);
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
    // 异步保存
    @Async
    public void saveRecord(AssessmentVO assessment, AssessmentRequestDTO dto) {
        AssessmentRecord record = new AssessmentRecord();
        record.setShareId(assessment.getShareId());
        record.setScore(assessment.getScore());
        record.setGender(assessment.getGender());
        record.setReport(assessment.getReport());
        record.setRadarData(JSON.toJSONString(assessment.getRadar()));
        record.setRawInput(JSON.toJSONString(dto));
        record.setLieFactor(assessment.getLieFactor());
        record.setMarketLevel(assessment.getMarketLevel());
        // 解析IP地址
        String ip = IPUtils.getRealIp(servletRequest);
        log.info("ip=" + ip);
        String region = geoLocationService.getRegionByIp(ip);
        String isp = "unknown isp";
        if(IPUtils.isIPv4(ip)) {
            log.info("regionStr=" + region);

            // 2. 优雅切分字符串
            String[] parts = region.split("\\|");

            String country = parts[0];
            String province = parts[2];
            String city = parts[3];
            isp = parts[4];

            region = "0".equals(province) ? country : province + " " + city;

        }
        record.setRegion(region);
        record.setIp(ip);
        record.setIsp(isp);
        record.setAddress(aMapComponent.getDetailAddress(dto.getLongitude(), dto.getLatitude()));

        assessmentMapper.insert(record);

   }

    /**
     * 非线性处理核心指标：年薪、身高
     */
    private Double calculateHardAssetScore(AssessmentRequestDTO req, boolean isMale) {
        double score = 0.0;

        // 身高：采用阈值逻辑。男性低于170或女性低于155，分值呈指数级下降
        if (isMale) {
            score += req.getHeight() < 170? Math.pow(req.getHeight()/170.0, 3) * 50 : req.getHeight() - 120;
        } else {
            score += req.getHeight() < 158? Math.pow(req.getHeight()/158.0, 2) * 60 : req.getHeight() - 100;
        }

        // 年薪：采用对数增长逻辑，解决年薪10万与100万之间线性权重失效问题
        // 使用 log10 处理分值，平滑高收入带来的分值溢出
        score += Math.log10(req.getAnnualIncome() / 100.0 + 1) * (isMale? 15 : 5);

        // 稳定性与房产
        score += req.getHouseStatus() * (isMale? 15 : 5);
        score += req.getWorkStability() * 5;

        return score;
    }

    /**
     * 数据置信度计算：逻辑冲突监测
     */
    private Double calculateConfidenceFactor(AssessmentRequestDTO req) {
        double confidence = 1.0;

        // 冲突1：视觉身高与净身高差距过大
        if (Math.abs(req.getVisualHeight() - req.getHeight()) > 10) {
            confidence -= 0.15; // 疑似注水
        }

        // 冲突2：朋友圈美化率极高 + 审美/穿搭预算极低
        if (req.getSocialFilters() >= 4 && req.getFashionInvestment() < 0.05) {
            confidence -= 0.1; // 社交形象可能存在过度包装
        }

        // 冲突3：回复延迟极低（秒回）+ 任性值极高
        // 真实高分个体通常伴随一定的社交距离感
        if (req.getReplyLatency() == 1 && req.getStubbornness() >= 8) {
            confidence -= 0.05;
        }

        // 冲突4：年薪极高但汽车/房产等级极低
        if (req.getAnnualIncome() > 100000000 && req.getHouseStatus() == 0) {
            confidence -= 0.2; // 财务数据疑似虚填
        }

        return Math.max(0.6, confidence);
    }

    private Double calculateSoftPowerScore(AssessmentRequestDTO req, boolean isMale) {
        // 软实力：知识面、厨艺、家务等线性累加
        return (double) (req.getTalkBreadth() * 4 + req.getCookingSkill() * 2 + req.getAestheticStyle() * 3);
    }

    private Double calculateMentalScore(AssessmentRequestDTO req, boolean isMale) {
        // 心理稳态得分
        return (double) (req.getEmotionalStability() * 10 - req.getControlDesire() * 2 + req.getEmpathyLevel() * 5);
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
        double rawAes = (dto.getAestheticStyle() * 10) + (dto.getPhotoSkill() * 6)
                + (dto.getFashionInvestment() * 6) + (dto.getTravelPlanning() * 4)
                + (dto.getSocialFilters() * 2);
        m.aesthetic = softenScore(rawAes, 115);

        // --- 4. 情绪带宽 (Emotional) ---
        double rawEmo = (dto.getEmpathyLevel() * 10) + (dto.getEmotionalStability() * 10)
                + (dto.getSharingDesire() * 6) + (dto.getPetType() * 3)
                + (dto.getRelationshipGoal() == 1 ? 8 : 4);
        m.emotional = softenScore(rawEmo, 120);

        // --- 5. 社交博弈 (Social) ---
        double rawSoc = (dto.getTalkBreadth() * 10) + (dto.getExtraSkills() * 6)
                + (dto.getCookingSkill() * 5) + (dto.getHouseworkLevel() * 5)
                + (20 - dto.getReplyLatency() * 4);
        m.social = softenScore(rawSoc, 105);

        // --- 6. 维护成本 (Maintenance) ---
        double rawMain = (10 - dto.getStubbornness()) * 4
                + (5 - dto.getExBonding()) * 6
                + (5 - dto.getColdViolenceProb() * 1.5) * 6
                + (10 - dto.getControlDesire() * 2)
                + (dto.getConsumptionView() == 2 ? 8 : 4);
        m.maintenance = softenScore(rawMain, 95);

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

    /**
     * 拉开中段分差：60~80 区间更敏感，避免“大家都差不多”
     */
    private double spreadScore(double raw) {
        double clamped = Math.max(0, Math.min(100, raw));
        double centered = (clamped - 50.0) / 15.0;
        double mapped = 50.0 + 45.0 * Math.tanh(centered);
        return Math.max(0, Math.min(100, mapped));
    }

    /**
     * Z-Score 拉开普通区间：避免 70-80 扎堆
     */
    private double zScoreSpread(double raw) {
        double clamped = Math.max(0, Math.min(100, raw));
        // 经验先验：真实样本均值约58，std约12；后续可切换为在线统计
        double z = (clamped - 58.0) / 12.0;
        double mapped = 50.0 + z * 18.0;
        return Math.max(0, Math.min(100, spreadScore(mapped)));
    }

    /**
     * 年龄-性别动态归一：同样收入在不同年龄段价值不同
     */
    private double incomeContextScore(long incomeFen, int age, String gender) {
        double incomeWan = incomeFen / 100.0 / 10000.0;
        double medianWan;
        if ("MALE".equals(gender)) {
            if (age <= 25) medianWan = 15;
            else if (age <= 30) medianWan = 22;
            else if (age <= 35) medianWan = 30;
            else if (age <= 40) medianWan = 38;
            else medianWan = 45;
        } else {
            if (age <= 25) medianWan = 13;
            else if (age <= 30) medianWan = 18;
            else if (age <= 35) medianWan = 24;
            else if (age <= 40) medianWan = 30;
            else medianWan = 36;
        }
        double ratio = incomeWan / Math.max(1.0, medianWan);
        // Sigmoid 映射到 0-100，ratio=1 时接近 50
        return 100.0 / (1.0 + Math.exp(-3.2 * (ratio - 1.0)));
    }

    /**
     * 特征交互项：避免简单加法导致“同分不同人”
     */
    private double calculateInteractionBonus(AssessmentRequestDTO dto, BaseMatrix m, double incomeContextScore) {
        double incomeTalk = (incomeContextScore / 100.0) * (dto.getTalkBreadth() / 5.0) * 12.0;
        double visualComposite = (m.biological / 100.0) * (m.aesthetic / 100.0) * 8.0;
        double stableAsset = (m.assets / 100.0) * (dto.getEmotionalStability() / 5.0) * 10.0;
        double softPowerGate = (dto.getPhotoSkill() + dto.getCookingSkill() + dto.getTravelPlanning()) / 12.0;
        double leisureSignal = (incomeContextScore / 100.0) * softPowerGate * 8.0;
        return incomeTalk + visualComposite + stableAsset + leisureSignal;
    }

    /**
     * 稀缺性溢价：稀缺但高价值行为额外加分
     */
    private double calculateScarcityPremium(AssessmentRequestDTO dto, String gender) {
        double premium = 0.0;
        if ("MALE".equals(gender)) {
            if (dto.getHouseworkLevel() >= 4 && dto.getEmpathyLevel() >= 4) premium += 6.0;
            if (dto.getCookingSkill() >= 3 && dto.getControlDesire() <= 2) premium += 3.5;
        } else {
            if (dto.getWorkStability() >= 4 && dto.getEmotionalStability() >= 4) premium += 4.5;
            if (dto.getTalkBreadth() >= 4 && dto.getStubbornness() <= 4) premium += 3.0;
        }
        return premium;
    }

    /**
     * 指数惩罚：致命缺陷不做线性扣分
     */
    private double exponentialPenalty(AssessmentRequestDTO dto) {
        int n = 0;
        n += Math.max(0, dto.getColdViolenceProb() - 4);
        n += Math.max(0, dto.getExBonding() - 4);
        n += Math.max(0, dto.getReplyLatency() - 3);
        return Math.pow(0.8, n);
    }

    /**
     * 亲密关系子模型：增强“爱”在男女模型中的区分度
     */
    private double calculateRelationshipScore(AssessmentRequestDTO dto, String gender) {
        double empathy = dto.getEmpathyLevel() * 20.0;
        double stability = dto.getEmotionalStability() * 20.0;
        double exRisk = dto.getExBonding() * 12.0;
        double coldRisk = dto.getColdViolenceProb() * 14.0;
        double controlRisk = dto.getControlDesire() * 10.0;
        double response = (5 - dto.getReplyLatency()) * 10.0;
        double sharing = dto.getSharingDesire() * 8.0;
        double goal = dto.getRelationshipGoal() == 4 ? 12.0 : 6.0;

        double score;
        if ("MALE".equals(gender)) {
            // 对男性更重“稳定表达 + 责任感”，冷暴力罚得更重
            score = empathy * 0.25
                    + stability * 0.30
                    + response * 0.20
                    + sharing * 0.10
                    + goal * 0.15
                    - exRisk * 0.40
                    - coldRisk * 0.60
                    - controlRisk * 0.25;
        } else {
            // 对女性更重“边界与长期协同”，控制欲和前任纠缠罚得更重
            score = empathy * 0.30
                    + stability * 0.25
                    + response * 0.15
                    + sharing * 0.15
                    + goal * 0.15
                    - exRisk * 0.55
                    - coldRisk * 0.35
                    - controlRisk * 0.45;
        }
        return Math.max(0, Math.min(100, score));
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
