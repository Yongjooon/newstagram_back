package com.ssafy.newstagram.api.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String accessToken;
    private final String refreshToken;
    private final String role;

    public LoginResponseDto(String accessToken, String refreshToken, String role){
        this.accessToken = "Bearer " + accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }
}
