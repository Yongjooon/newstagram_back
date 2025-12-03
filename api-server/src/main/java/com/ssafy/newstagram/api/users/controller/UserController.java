package com.ssafy.newstagram.api.users.controller;

import com.ssafy.newstagram.api.auth.model.dto.LoginRequestDto;
import com.ssafy.newstagram.api.auth.model.dto.LoginResponseDto;
import com.ssafy.newstagram.api.users.model.dto.RegisterRequestDto;
import com.ssafy.newstagram.api.users.model.service.UserService;
import com.ssafy.newstagram.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "UserController", description = "회원 관리를 위한 기능 제공")
public class UserController {

    private final UserService userService;

    @PostMapping("/email")
    @Operation(summary = "일반 회원가입", description = "이메일 기반으로 일반 회원가입을 요청한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공 - 사용자 생성됨"),
            @ApiResponse(responseCode = "400", description = "회원가입 실패 - 잘못된 요청 데이터 형식"),
    })
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

}
