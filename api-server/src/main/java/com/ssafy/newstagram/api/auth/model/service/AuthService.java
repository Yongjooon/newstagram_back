package com.ssafy.newstagram.api.auth.model.service;

import com.ssafy.newstagram.api.auth.model.dto.RefreshTokenRequestDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void refresh(RefreshTokenRequestDto dto);
    void logout(Long userId);
}
