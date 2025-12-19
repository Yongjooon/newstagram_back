package com.ssafy.newstagram.api.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class TokenValidationRequestDto {
    @Schema(description = "Authorization (Bearer {액세스 토큰})", example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3NjYxMTQ2MDMsImV4cCI6MTc2NjIwMTAwM30.HMsQoMxmxsCOscLa0Rrp-ltfzz0hHW2tmfgrR0oot8s")
    private String authorization;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1IiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3NjUzMjk2MTcsImV4cCI6MTc2NTkzNDQxN30.f5MdQaZGFHSWToJKF9F0wx30xKqU5XVm0qvNySUs5gw")
    private String refreshToken;
}
