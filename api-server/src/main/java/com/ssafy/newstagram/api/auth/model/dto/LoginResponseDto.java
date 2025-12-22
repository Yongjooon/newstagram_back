package com.ssafy.newstagram.api.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String accessToken;
    private final String refreshToken;
    private final boolean isInitialized;

    public LoginResponseDto(String accessToken, String refreshToken){
        this.accessToken = "Bearer " + accessToken;
        this.refreshToken = refreshToken;
        this.isInitialized = true;
    }

    public LoginResponseDto(String accessToken, String refreshToken, boolean isInitialized) {
        this.accessToken = "Bearer " + accessToken;
        this.refreshToken = refreshToken;
        this.isInitialized = isInitialized;
    }
}
