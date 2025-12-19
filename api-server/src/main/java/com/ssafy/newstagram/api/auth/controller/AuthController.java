package com.ssafy.newstagram.api.auth.controller;

import com.ssafy.newstagram.api.auth.jwt.JWTUtil;
import com.ssafy.newstagram.api.auth.model.dto.*;
import com.ssafy.newstagram.api.auth.model.service.AuthService;
import com.ssafy.newstagram.api.auth.model.service.RefreshTokenService;
import com.ssafy.newstagram.api.auth.model.service.VerificationCodeService;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.exception.TokenException;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "인증/인가를 위한 기능 제공")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;
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

    @PostMapping("/password/reset-request")
    @Operation(
            summary = "비밀번호 재설정 요청",
            description =
                    "이메일을 입력받아 비밀번호 재설정 링크를 발송합니다.\n\n"
                            + "- 입력한 이메일이 존재하지 않더라도 동일한 응답을 반환합니다. (보안 목적)\n"
                            + "- 비밀번호 재설정 토큰은 Redis에 저장되며 일정 시간(1시간) 후 만료됩니다.\n"
                            + "- 사용자는 이메일로 전달받은 링크를 통해 비밀번호를 재설정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 요청 성공 (이메일 발송 처리 완료)"
            )
    })
    public ResponseEntity<?> passwordResetRequest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "비밀번호 재설정 요청을 요청하는 정보",
                    required = true
            )
            @RequestBody PasswordResetRequestRequestDto dto
    ) {
        authService.requestPasswordReset(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "비밀번호 재설정 요청 성공",
                        Map.of("email", dto.getEmail())
                )
        );
    }

    @PostMapping("/password/reset")
    @Operation(
            summary = "비밀번호 재설정",
            description =
                    "비밀번호 재설정 토큰을 검증한 뒤 새로운 비밀번호로 변경합니다.\n\n"
                            + "- 이메일로 전달된 토큰을 사용합니다.\n"
                            + "- 토큰이 유효하지 않거나 만료된 경우 실패합니다.\n"
                            + "- 비밀번호 변경이 완료되면 해당 토큰은 즉시 폐기됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않거나 만료된 토큰"
            )
    })
    public ResponseEntity<?> passwordReset(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "비밀번호 재설정 요청 정보",
                    required = true
            )
            @RequestBody PasswordResetRequestDto dto
    ) {
        authService.passwordReset(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData(
                        "AUTH_200",
                        "비밀번호 재설정 성공"
                )
        );
    }

    @PostMapping("/email/find/request")
    @Operation(
            summary = "이메일 찾기 인증번호 요청",
            description = "휴대폰 번호를 입력받아 이메일 찾기용 인증번호를 SMS로 전송합니다.\n\n" +
                    "- 가입된 휴대폰 번호가 존재하는 경우에만 인증번호가 전송됩니다.\n" +
                    "- 가입된 휴대폰 번호가 존재하지 않는 경우에는, 인증번호는 전송되지 않지만 보안을 위하여 동일한 응답을 보냅니다.\n" +
                    "- 인증번호의 유효 시간은 5분입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 찾기 요청 성공"
            )
    })
    public ResponseEntity<?> requestEmailFind(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이메일 찾기 요청 정보",
                    required = true
            )
            @Valid @RequestBody EmailFindRequestDto dto
    ) {
        final long expirationMs = 300000;
        verificationCodeService.requestEmailFindVerificationCode(dto, expirationMs);

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "이메일 찾기 요청 성공",
                        Map.of(
                                "message", dto.getPhoneNumber() + "로 인증번호가 전송되었습니다.",
                                "expiresIn", expirationMs / 1000
                        )
                )
        );
    }

    @PostMapping("/email/find/verify")
    @Operation(
            summary = "이메일 찾기 인증번호 검증",
            description = "휴대폰 번호와 인증번호를 검증하여 해당 사용자의 이메일을 반환합니다.\n\n" +
                    "- 인증 성공 시 이메일이 반환됩니다.\n" +
                    "- 인증번호가 틀리거나 만료된 경우 에러가 발생합니다.\n" +
                    "- 하나의 인증번호에 대하여, 최대 5번의 요청이 가능하고 초과 시에는 해당 인증번호를 만료시킵니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 찾기 성공"
            )
    })
    public ResponseEntity<?> verifyEmailFind(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이메일 찾기 인증 요청 정보",
                    required = true
            )
            @Valid @RequestBody EmailFindVerifyRequestDto dto
    ) {
        String email = verificationCodeService.verifyAndGetEmail(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "이메일 찾기 성공",
                        Map.of(
                                "email", email
                        )
                )
        );
    }

    @PostMapping("/signup/phone-verification/request")
    @Operation(
            summary = "회원가입 휴대폰 인증번호 요청",
            description =
                    "회원가입을 위해 휴대폰 번호로 인증번호를 전송합니다.\n\n" +
                            "- 입력한 휴대폰 번호로 인증번호(SMS)가 발송됩니다.\n" +
                            "- 인증번호는 일정 시간(5분) 동안만 유효합니다.\n" +
                            "- 동일 휴대폰 번호로 여러 번 요청 시, 기존 인증번호는 무효화됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "휴대폰 인증번호 전송 성공"
            )
    })
    public ResponseEntity<?> requestPhoneVerification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "휴대폰 인증번호 요청 정보",
                    required = true
            )
            @Valid @RequestBody PhoneVerificationRequestDto dto
    ) {
        final long expirationMs = 300000;
        verificationCodeService.requestPhoneVerificationCode(dto, expirationMs);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "휴대폰번호 인증 요청 성공",
                        Map.of(
                                "message", dto.getPhoneNumber() + "로 인증번호가 전송되었습니다. 1분 이상 인증번호가 오지 않는다면, 휴대폰번호를 확인해보시고 재요청해주세요.",
                                "expiresIn", expirationMs / 1000
                        )
                )
        );
    }

    @PostMapping("/signup/phone-verification/verify")
    @Operation(
            summary = "회원가입 휴대폰 인증번호 검증",
            description =
                    "휴대폰 번호와 인증번호를 검증하여 회원가입을 위한 휴대폰 인증을 완료합니다.\n\n" +
                            "- 인증번호가 일치하고 유효한 경우 인증이 완료됩니다.\n" +
                            "- 인증번호가 틀리거나 만료된 경우 에러가 발생합니다.\n" +
                            "- 하나의 인증번호에 대하여, 최대 5번의 요청이 가능하고 초과 시에는 해당 인증번호를 만료시킵니다.\n" +
                            "- 인증 성공 시, 해당 휴대폰 번호는 회원가입에 사용할 수 있습니다.\n" +
                            "- 인증 완료 여부는 1시간 동안 저장되므로, 인증 후 1시간 이내에 회원가입을 완료해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "휴대폰 인증 성공"
            )
    })
    public ResponseEntity<?> verifyPhoneVerification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "휴대폰 인증번호 검증 정보",
                    required = true
            )
            @Valid @RequestBody PhoneVerificationConfirmDto dto
    ) {
        verificationCodeService.confirmPhoneVerification(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData(
                        "AUTH_200",
                        "휴대폰번호 인증 성공"
                )
        );
    }

    @PostMapping("/token")
    @Operation(
            summary = "토큰 유효성 검사 및 재발급",
            description = "클라이언트가 Access Token과 Refresh Token을 전달하면 다음과 같이 처리합니다.\n\n"
        + "- Access Token이 유효한 경우 → accesstokenOK = true, 액세스 토큰 그대로 사용\n"
        + "- Access Token이 만료된 경우 → accesstokenOK = false, Refresh Token으로 재발급된 액세스 토큰 전달\n"
        + "- 두 토큰 모두 유효하지 않은 경우 → 401 Unauthorized 반환\n\n"
        + "※ Authorization 헤더는 `Bearer {accessToken}` 형식이어야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 유효성 검사 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "토큰이 유효하지 않음 또는 인증 실패"
            )
    })
    public ResponseEntity<?> validateToken(
            @Parameter(
                    description = "Access Token (Bearer 타입)",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    required = false
            )
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(
                    description = "Refresh Token",
                    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    required = false
            )
            @RequestHeader(value = "refreshToken", required = false) String refreshToken
    ){
        TokenValidationRequestDto dto = new TokenValidationRequestDto(authorization, refreshToken);
        TokenValidationResponseDto result = authService.validateToken(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "AUTH_200",
                        "토큰 유효성 검사 성공",
                        result
                )
        );
    }
}
