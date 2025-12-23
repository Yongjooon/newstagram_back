package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class PasswordResetRequestRequestDto {
    @Schema(description = "이메일", example = "test@example.com")
    @ValidEmail
    private String email;
}
