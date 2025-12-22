package com.ssafy.newstagram.api.users.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePasswordRequestDto {

    @Schema(description = "기존 비밀번호", example = "password1234")
    @ValidPassword
    private String currentPassword;

    @Schema(description = "새로운 비밀번호", example = "newpassword1234")
    @ValidPassword
    private String newPassword;

}
