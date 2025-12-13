package com.ssafy.newstagram.api.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    public void save(Long userId, String refreshToken, long ttl){
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + String.valueOf(userId),
                refreshToken,
                ttl,
                TimeUnit.MILLISECONDS
        );
    }

    public void delete(Long userId){
        String key = REFRESH_TOKEN_PREFIX + String.valueOf(userId);
        redisTemplate.delete(key);
    }

    public String getRefreshToken(Long userId){
        String key = REFRESH_TOKEN_PREFIX + String.valueOf(userId);
        return (String) redisTemplate.opsForValue().get(key);
    }
}
