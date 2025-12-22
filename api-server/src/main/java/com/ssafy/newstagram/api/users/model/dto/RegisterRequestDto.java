package com.ssafy.newstagram.api.users.model.dto;


import com.ssafy.newstagram.api.users.validation.ValidNickname;
import com.ssafy.newstagram.api.users.validation.ValidPassword;
import com.ssafy.newstagram.api.users.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterRequestDto {

    @Schema(description = "휴대폰번호", example = "01012345678")
    @NotBlank
    @ValidPhoneNumber
    private String phoneNumber;

    @Schema(description = "이메일", example = "test@example.com")
    @Pattern(
            regexp = "^[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다."
    )
    @NotBlank
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    @ValidPassword
    private String password;

    @Schema(description = "닉네임", example = "닉네임예시")
    @ValidNickname
    private String nickname;
}
