package com.ssafy.newstagram.api.auth.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordResetRequestRequestDto {
    @Pattern(
            regexp = "^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    @NotBlank
    private String email;
}
