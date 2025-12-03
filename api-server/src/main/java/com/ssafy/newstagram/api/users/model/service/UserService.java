package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.users.model.dto.RegisterRequestDto;

public interface UserService {
    void register(RegisterRequestDto user);
    LoginResponseDto login(LoginRequestDto user);
}
