package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.entity.AssessmentRecord;
import com.nextify.blog.vo.AssessmentVO;

public interface AssessmentShareService extends IService<AssessmentRecord> {

    String saveRecord(AssessmentRequestDTO inputs, AssessmentVO result, String gender);
}
