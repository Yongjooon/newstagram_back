package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.TokenValidationRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.TokenValidationResponseDto;
import jakarta.servlet.http.HttpServletResponse;

import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestRequestDto;

public interface AuthService {
    void logout(Long userId);
    void requestPasswordReset(PasswordResetRequestRequestDto dto);
    void passwordReset(PasswordResetRequestDto dto);
    TokenValidationResponseDto validateToken(TokenValidationRequestDto dto);
}
