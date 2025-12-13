package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.jwt.RefreshTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenUtil refreshTokenUtil;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_TOKEN_TTL;

    public void save(Long userId, String refreshToken){
        refreshTokenUtil.save(userId, refreshToken, REFRESH_TOKEN_TTL);
    }
    public String getRefreshToken(Long userId){
        return refreshTokenUtil.getRefreshToken(userId);
    }

    public void delete(Long userId){
        refreshTokenUtil.delete(userId);
    }
}
