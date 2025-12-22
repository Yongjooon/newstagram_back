package com.ssafy.newstagram.api.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class LoginResponseDto {
    private final String accessToken;
    private final String refreshToken;

    public LoginResponseDto(String accessToken, String refreshToken){
        this.accessToken = "Bearer " + accessToken;
        this.refreshToken = refreshToken;
    }
}
