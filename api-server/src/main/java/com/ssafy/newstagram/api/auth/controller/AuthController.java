package com.ssafy.newstagram.api.auth.controller;

import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.auth.model.service.AuthService;
import com.ssafy.newstagram.api.auth.model.service.RefreshTokenService;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.auth.model.dto.RefreshTokenRequestDto;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.api.users.model.service.UserService;
import com.ssafy.newstagram.domain.user.entity.User;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "인증/인가를 위한 기능 제공")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    @PostMapping("/login")
    @Operation(
            summary = "일반 로그인",
            description = "이메일과 비밀번호로 로그인합니다.\n\n"
                    + "- 이 API는 Swagger 문서화를 위한 가짜 엔드포인트입니다.\n"
                    + "- 실제 로그인 처리는 Spring Security LoginFilter에서 수행됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 (Access/Refresh 토큰 발급)"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 오류")
    })
    public void login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 요청 정보",
                    required = true
            )
            @RequestBody LoginRequestDto dto
    ) {
        throw new UnsupportedOperationException("Swagger 문서용 API입니다.");
    }
    

    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.\n\n"
                    + "- 만료된 Access Token을 재발급할 때 사용됩니다.\n"
                    + "- Refresh Token이 유효하지 않거나 만료된 경우 재발급에 실패합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공"
            )
    })
    public ResponseEntity<?> refreshAccessToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh Token 정보",
                    required = true
            )
            @RequestBody RefreshTokenRequestDto dto,
            @Parameter(hidden = true)
            HttpServletResponse response
    ) {
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
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인된 사용자를 로그아웃 처리합니다.\n\n"
                    + "- JWT 인증이 필요합니다.\n"
                    + "- 서버에 저장된 Refresh Token이 무효화됩니다.\n"
                    + "- 클라이언트는 저장된 토큰을 삭제해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            )
    })
    public ResponseEntity<?> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        authService.logout(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData(
                        "AUTH_200",
                        "로그아웃 성공"
                )
        );
    }
}
