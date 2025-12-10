package com.ssafy.newstagram.api.auth.controller;

import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.auth.model.service.AuthService;
import com.ssafy.newstagram.api.auth.model.service.RefreshTokenService;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.auth.model.dto.RefreshTokenRequestDto;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.api.users.model.service.UserService;
import com.ssafy.newstagram.domain.user.entity.User;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "인증/인가를 위한 기능 제공")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequestDto dto, HttpServletResponse response){
//        authService.refresh(dto);

        String refreshToken = dto.getRefreshToken();

        Long userId = jwtUtil.getUserId(refreshToken);
        String type = jwtUtil.getType(refreshToken);

        if(userId == null){
            throw new JwtException("Invalid refresh token: subject missing");
        }
        if(type == null || !type.equals("refresh")){
            throw new JwtException("Invalid refresh token: type claim invalid");
        }

        User user = userService.getUserById(userId);
        String redisRefreshToken = refreshTokenService.getRefreshToken(userId);
        if(user == null || redisRefreshToken == null || !redisRefreshToken.equals(refreshToken)){
            throw new JwtException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.createAccessToken(userId, user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(userId);

        refreshTokenService.save(userId, newRefreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "토큰 재발급 성공",
                        new LoginResponseDto(newAccessToken, newRefreshToken)
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails){
        authService.logout(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData(
                        "AUTH_200",
                        "로그아웃 성공"
                )
        );
    }
}
