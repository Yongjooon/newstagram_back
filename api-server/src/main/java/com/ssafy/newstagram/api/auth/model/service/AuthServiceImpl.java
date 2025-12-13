package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.RefreshTokenRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    @Override
    public void refresh(RefreshTokenRequestDto dto) {}

    @Override
    public void logout(Long userId) {
        refreshTokenService.delete(userId);
    }
}
