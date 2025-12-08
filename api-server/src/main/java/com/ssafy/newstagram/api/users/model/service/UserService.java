package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.users.model.dto.RegisterRequestDto;
import com.ssafy.newstagram.api.users.model.dto.UserInfoDto;

public interface UserService {
    void register(RegisterRequestDto user);
    LoginResponseDto login(LoginRequestDto user);
    void deleteUserByEmail(String email);
    UserInfoDto getUserInfoByEmail(String email);
}
