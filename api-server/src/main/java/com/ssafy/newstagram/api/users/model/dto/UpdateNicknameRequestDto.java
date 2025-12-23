package com.ssafy.newstagram.api.users.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidNickname;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateNicknameRequestDto {
    @Schema(description = "새로운 닉네임", example = "새닉네임예시")
    @ValidNickname
    private String newNickname;
}
