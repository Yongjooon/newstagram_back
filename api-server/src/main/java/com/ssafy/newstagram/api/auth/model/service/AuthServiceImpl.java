package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestRequestDto;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String TOKEN_PREFIX = "password-reset:";
    private final long expirationMs = 3600000; // 1시간

    @Override
    public void requestPasswordReset(PasswordResetRequestRequestDto dto) {
        String email = dto.getEmail();

        User user = userRepository.findByEmail(email);
        if(user == null){
            return;
        }

        String token = issueToken();

        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + token,
                user.getId(),
                expirationMs,
                TimeUnit.MILLISECONDS
        );

        emailService.sendPasswordResetEmail(email, token);
    }

    private String issueToken(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
