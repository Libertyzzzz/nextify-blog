package com.nextify.blog.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 处理认证失败 (401)
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        Result<Object> result = Result.fail(ResultCode.UNAUTHORIZED);
        String json = new ObjectMapper().writeValueAsString(result);
        response.getWriter().write(json);
    }
}