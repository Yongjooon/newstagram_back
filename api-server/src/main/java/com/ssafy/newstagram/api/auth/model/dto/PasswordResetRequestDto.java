package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordResetRequestDto {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자리여야 합니다.")
    @ValidPassword
    private String newPassword;
}
