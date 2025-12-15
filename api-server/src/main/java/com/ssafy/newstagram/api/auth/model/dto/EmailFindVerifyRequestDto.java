package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.EmailFindCode;
import com.ssafy.newstagram.api.users.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailFindVerifyRequestDto {
    @Schema(description = "휴대폰번호", example = "01012345678")
    @NotBlank
    @ValidPhoneNumber
    private String phoneNumber;

    @Schema(description = "인증번호")
    @NotBlank
    @EmailFindCode
    private String verificationCode;
}
