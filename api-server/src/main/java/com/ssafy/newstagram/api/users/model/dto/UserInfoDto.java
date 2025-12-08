package com.ssafy.newstagram.api.users.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserInfoDto {
    private final String email;
    private final String nickname;
    private final String role;
    private final List<Double> preferenceEmbedding;
}
