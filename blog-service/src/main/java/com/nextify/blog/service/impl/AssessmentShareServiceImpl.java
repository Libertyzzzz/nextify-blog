package com.nextify.blog.service.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.entity.AssessmentRecord;
import com.nextify.blog.mapper.AssessmentRecordMapper;
import com.nextify.blog.service.AssessmentShareService;
import com.nextify.blog.vo.AssessmentVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AssessmentShareServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord> implements AssessmentShareService {
    private static final String SEED = "23456789abcdefghjkmnpqrstuvwxyz";
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private AssessmentRecordMapper assessmentRecordMapper;
    @Override
    public String saveRecord(AssessmentRequestDTO inputs, AssessmentVO result, String gender)  {
        String shareId = NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                SEED.toCharArray(),
                8

        );
        AssessmentRecord entity = new AssessmentRecord();
        entity.setShareId(shareId);
        entity.setGender(gender);
        entity.setScore(result.getScore());
        entity.setMarketLevel(result.getMarketLevel());
        entity.setReport(result.getReport());

        // 使用 Jackson 将 Map 转为 JSON 字符串
        try{
            entity.setRadarData(objectMapper.writeValueAsString(result.getRadar()));
            entity.setRawInput(objectMapper.writeValueAsString(inputs));
        } catch (JsonProcessingException exception){
            throw new RuntimeException("json parsed error", exception);
        }


        this.save(entity);
        return shareId;


    }
}

