package com.ssafy.newstagram.api.users.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateNicknameRequestDto {

    @NotBlank
    @Size(min = 2, message = "닉네임은 최소 2글자여야 합니다.")
    private String newNickname;

}
