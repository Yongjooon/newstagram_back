package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidEmail;
import com.ssafy.newstagram.api.users.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @Schema(description = "이메일", example = "test@example.com")
    @ValidEmail
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    @ValidPassword
    private String password;
}
