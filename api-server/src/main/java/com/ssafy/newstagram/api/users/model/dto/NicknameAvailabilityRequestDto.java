package com.ssafy.newstagram.api.users.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class NicknameAvailabilityRequestDto {
    @Schema(description = "닉네임", example = "닉네임예시")
    @ValidNickname
    private String nickname;
}
