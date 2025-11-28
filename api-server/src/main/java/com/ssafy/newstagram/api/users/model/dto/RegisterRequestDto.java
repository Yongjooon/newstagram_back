package com.ssafy.newstagram.api.users.model.dto;


import com.ssafy.newstagram.api.users.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterRequestDto {

    @Pattern(
            regexp = "^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자리여야 합니다.")
    @ValidPassword
    private String password;

    @NotBlank
    @Size(min = 2, message = "닉네임은 최소 2글자여야 합니다.")
    private String nickname;
}
