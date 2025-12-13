package com.ssafy.newstagram.api.users.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateNicknameRequestDto {
    @Schema(description = "새로운 닉네임", example = "새닉네임예시")
    @NotBlank
    @Size(min = 2, message = "닉네임은 최소 2글자여야 합니다.")
    private String newNickname;

}
