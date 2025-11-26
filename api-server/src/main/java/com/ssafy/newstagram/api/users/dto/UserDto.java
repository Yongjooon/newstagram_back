package com.ssafy.newstagram.api.users.dto;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private String password; // password_hash가 아니라 password
    private String nickname;
    private String loginType; // enum??
    private String providerId;
    private String role; // enum??
    private String createdAt;
    private String updatedAt;
}
