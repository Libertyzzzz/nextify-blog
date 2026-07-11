package com.nextify.blog.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 处理权限不足 (403)
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 如果是匿名用户，说明是 Token 失效或没登录，报 401
        ResultCode code = (auth instanceof AnonymousAuthenticationToken || auth == null) ? ResultCode.UNAUTHORIZED : ResultCode.FORBIDDEN;
        
        Result<Object> result = Result.fail(code);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}