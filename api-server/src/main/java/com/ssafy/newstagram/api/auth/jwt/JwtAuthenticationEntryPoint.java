package com.ssafy.newstagram.api.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.common.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type("AUTHENTICATION_FAILED")
                .message("유효하지 않은 토큰입니다.")
                .build();

        BaseResponse<?> errorResponse = BaseResponse.error("AUTH_401", "인증 실패", errorDetail);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
