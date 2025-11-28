package com.ssafy.newstagram.api.users.model.service;

import com.ssafy.newstagram.api.users.model.dto.RegisterRequestDto;
import com.ssafy.newstagram.domain.user.entity.User;

public interface UserService {
    void register(RegisterRequestDto user);
}
