package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.EmailFindRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.EmailFindVerifyRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.PhoneVerificationRequestDto;

public interface VerificationCodeService {
    void requestEmailFindVerificationCode(EmailFindRequestDto dto, long expirationMs);

    String verifyAndGetEmail(EmailFindVerifyRequestDto dto);

    void requestPhoneVerificationCode(PhoneVerificationRequestDto dto, long expirationMs);
}
