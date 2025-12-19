package com.ssafy.newstagram.api.auth.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class TokenValidationResponseDto {
    private final String accessToken;
    private final boolean accessTokenOK;

    public TokenValidationResponseDto(String accessToken, boolean accessTokenOK){
        this.accessToken = "Bearer " + accessToken;
        this.accessTokenOK = accessTokenOK;
    }
}
