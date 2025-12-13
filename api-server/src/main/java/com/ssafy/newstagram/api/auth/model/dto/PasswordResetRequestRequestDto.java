package com.ssafy.newstagram.api.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordResetRequestRequestDto {
    @Schema(description = "이메일", example = "test@example.com")
    @Pattern(
            regexp = "^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    @NotBlank
    private String email;
}
