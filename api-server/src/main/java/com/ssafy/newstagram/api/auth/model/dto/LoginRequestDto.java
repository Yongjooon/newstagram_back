package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @Schema(description = "이메일", example = "test@example.com")
    @Pattern(
            regexp = "^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자리여야 합니다.")
    @ValidPassword
    private String password;
}
