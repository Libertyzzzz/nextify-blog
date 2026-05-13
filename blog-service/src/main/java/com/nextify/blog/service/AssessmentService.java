package com.nextify.blog.service;

import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.vo.AssessmentVO;

public interface AssessmentService {

    AssessmentVO evaluate(AssessmentRequestDTO dto, String gender);

    AssessmentVO findByShareId(String shareId);
}
