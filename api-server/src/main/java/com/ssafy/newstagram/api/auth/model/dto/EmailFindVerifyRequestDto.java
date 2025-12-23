package com.ssafy.newstagram.api.auth.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidPhoneVerificationCode;
import com.ssafy.newstagram.api.users.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class EmailFindVerifyRequestDto {
    @Schema(description = "휴대폰번호", example = "01012345678")
    @ValidPhoneNumber
    private String phoneNumber;

    @Schema(description = "인증번호")
    @ValidPhoneVerificationCode
    private String verificationCode;
}
