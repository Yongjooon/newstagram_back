package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.TokenValidationRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.TokenValidationResponseDto;
import com.ssafy.newstagram.api.exception.TokenException;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    private final String TOKEN_PREFIX = "password-reset:";
    private final long expirationMs = 3600000; // 1시간

    @Override
    public void logout(Long userId) {
        refreshTokenService.delete(userId);
    }
  
    @Override
    public void requestPasswordReset(PasswordResetRequestRequestDto dto) {
        String email = dto.getEmail();

        User user = userRepository.findByEmail(email).orElse(null);
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

    @Override
    @Transactional
    public void passwordReset(PasswordResetRequestDto dto) {
        String key = TOKEN_PREFIX + dto.getToken();
        Object value = redisTemplate.opsForValue().get(key);
        if(value == null){
            throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
        }

        long userId;
        try {
            userId = Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("토큰 데이터가 손상되었습니다.");
        }

        log.debug("[비밀번호 초기화 요청] id={}", value.toString());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newEncoded = passwordEncoder.encode(dto.getNewPassword());
        user.updatePasswordHash(newEncoded);

        redisTemplate.delete(key);
    }

    @Override
    public TokenValidationResponseDto validateToken(TokenValidationRequestDto dto) {
        String authorization = dto.getAuthorization();
        String refreshToken = dto.getRefreshToken();

        // accessToken 유효한 경우
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new TokenException("Authorization 헤더가 없거나 Bearer 토큰 형식이 아닙니다.");
            }
            String accessToken = authorization.split(" ")[1];
            if (!jwtUtil.isExpired(accessToken)) {
                return new TokenValidationResponseDto(accessToken, true);
            }
        } catch (Exception e) {
            // 토큰 파싱 자체가 안 되는 경우 => 아래에서 refresh로 처리
        }

        // accessToken 만료 => refreshToken 검증
        try {
            if(refreshToken == null){
                throw new TokenException("REFRESH_TOKEN_EMPTY");
            }
            if (jwtUtil.isExpired(refreshToken)) {
                throw new TokenException("REFRESH_TOKEN_EXPIRED");
            }

            if (!"refresh".equals(jwtUtil.getType(refreshToken))) {
                throw new TokenException("INVALID_REFRESH_TOKEN");
            }

            Long userId = jwtUtil.getUserId(refreshToken);
            String savedRefreshToken = refreshTokenService.getRefreshToken(userId);

            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                throw new TokenException("INVALID_REFRESH_TOKEN");
            }

            // 3. 재발급
            // String role = userRepository.findRoleById(userId);
            String role = "USER";
            String newAccessToken = jwtUtil.createAccessToken(userId, role);

            return new TokenValidationResponseDto(newAccessToken, false);

        } catch (Exception e) {
            throw new TokenException("REFRESH_TOKEN_EXPIRED");
        }
    }

    private String issueToken(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
