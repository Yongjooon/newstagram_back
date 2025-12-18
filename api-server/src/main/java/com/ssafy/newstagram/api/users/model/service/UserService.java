package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.users.model.dto.*;
import com.ssafy.newstagram.domain.user.entity.User;

public interface UserService {
    void register(RegisterRequestDto user);
    void deleteUserById(Long userId);
    UserInfoDto getUserInfoByUserId(Long userId);
    void updateNickname(Long userId, UpdateNicknameRequestDto dto);
    User getUserById(Long userId);
    void updatePassword(Long userId, UpdatePasswordRequestDto dto);

    boolean isAvailableEmail(EmailAvailabilityRequestDto dto);
    boolean isAvailablePhoneNumber(PhoneNumberAvailabilityRequestDto dto);
    boolean isAvailableNickname(NicknameAvailabilityRequestDto dto);
}
