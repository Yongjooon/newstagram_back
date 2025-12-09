package com.ssafy.newstagram.api.auth.model.dto;

import lombok.Getter;

@Getter
public class RefreshTokenRequestDto {
    private String refreshToken;
}
