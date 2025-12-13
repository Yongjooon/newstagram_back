package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordResetRequestDto {
    @Schema(description = "비밀번호 재설정 토큰", example = "e03b5b13246b4f21ae61280d1c2c3097")
    @NotBlank
    private String token;

    @Schema(description = "새로운 비밀번호", example = "newpassword1234")
    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자리여야 합니다.")
    @ValidPassword
    private String newPassword;
}
