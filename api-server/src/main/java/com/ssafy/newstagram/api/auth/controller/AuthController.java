package com.ssafy.newstagram.api.auth.controller;

import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.auth.model.dto.RefreshTokenRequestDto;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.api.users.model.service.UserService;
import com.ssafy.newstagram.domain.user.entity.User;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JWTUtil jwtUtil;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequestDto dto){

        String refreshToken = dto.getRefreshToken();

        if(refreshToken == null || refreshToken.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    BaseResponse.error(
                            "AUTH_400",
                            "리프레시 토큰 값은 필수입니다.",
                            null
                    )
            );
        }

        String email = jwtUtil.getEmail(refreshToken);
        String type = jwtUtil.getType(refreshToken);

        if(email == null){
            throw new JwtException("Invalid refresh token: email claim missing");
        }
        if(type == null || !type.equals("refresh")){
            throw new JwtException("Invalid refresh token: type claim invalid");
        }

        User user = userService.getUserByEmail(email);
        if(user == null || user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)){
            throw new JwtException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.createAccessToken(email, user.getRole());
        String newRefreshToken = jwtUtil.createRefreshToken(email);

        userService.updateRefreshToken(email, newRefreshToken);

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "토큰 재발급 성공",
                        new LoginResponseDto(newAccessToken, newRefreshToken)
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){

        // 현재 사용자의 인증 정보에서 email 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String email = userDetails.getUsername();

        userService.deleteRefreshToken(email);

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData(
                        "AUTH_200",
                        "로그아웃 성공"
                )
        );
    }
}
