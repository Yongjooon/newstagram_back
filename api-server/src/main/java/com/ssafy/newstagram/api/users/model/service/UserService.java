package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.users.model.dto.RegisterRequestDto;
import com.ssafy.newstagram.api.users.model.dto.UpdateNicknameRequestDto;
import com.ssafy.newstagram.api.users.model.dto.UserInfoDto;
import com.ssafy.newstagram.domain.user.entity.User;

public interface UserService {
    void register(RegisterRequestDto user);
    void deleteUserByEmail(String email);
    UserInfoDto getUserInfoByEmail(String email);
    void updateNickname(String email, UpdateNicknameRequestDto dto);
    User getUserByEmail(String email);
    void updateRefreshToken(String email, String newRefreshToken);
    void deleteRefreshToken(String email);
}
