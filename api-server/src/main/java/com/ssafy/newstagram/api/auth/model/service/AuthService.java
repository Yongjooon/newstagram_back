package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.PasswordResetRequestRequestDto;

public interface AuthService {
    void requestPasswordReset(PasswordResetRequestRequestDto dto);
//    void passwordReset(PasswordResetRequestDto dto);
}
