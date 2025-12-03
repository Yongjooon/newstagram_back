package com.ssafy.newstagram.api.users.controller;

import com.ssafy.newstagram.api.users.model.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "인증/인가를 위한 기능 제공")
public class AuthController {
    private final UserService userService;
}
