package com.ssafy.newstagram.api.users.model.dto;

import com.ssafy.newstagram.api.users.validation.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PhoneNumberAvailabilityRequestDto {
    @Schema(description = "휴대폰번호", example = "01012345678")
    @NotBlank
    @ValidPhoneNumber
    private String phoneNumber;
}
