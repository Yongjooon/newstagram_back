package com.ssafy.newstagram.api.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequestDto {
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1IiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3NjUzMjk2MTcsImV4cCI6MTc2NTkzNDQxN30.f5MdQaZGFHSWToJKF9F0wx30xKqU5XVm0qvNySUs5gw")
    @NotBlank(message = "리프레시 토큰 값은 필수입니다.")
    private String refreshToken;
}
