package com.ssafy.newstagram.api.auth.model.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                "RT:" + email,
                refreshToken,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    public String getRefreshToken(String email) {
        return (String) redisTemplate.opsForValue().get("RT:" + email);
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete("RT:" + email);
    }
}
