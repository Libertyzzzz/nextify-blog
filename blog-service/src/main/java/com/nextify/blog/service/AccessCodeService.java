package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextify.blog.dto.AccessCodeAddRequest;
import com.nextify.blog.dto.AssessmentRequestDTO;
import com.nextify.blog.entity.AccessCode;
import com.nextify.blog.vo.AccessCodeVo;
import com.nextify.blog.vo.AssessmentVO;

public interface AccessCodeService extends IService<AccessCode> {


    Boolean verify(Integer id, String accessCode);

    Integer save(AccessCodeAddRequest request);
}
