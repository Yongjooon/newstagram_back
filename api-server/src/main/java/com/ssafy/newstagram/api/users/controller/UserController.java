package com.ssafy.newstagram.api.users.controller;

import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.users.model.dto.*;
import com.ssafy.newstagram.api.users.model.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.successNoData("USER_201", "회원가입 성공")
        );
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원탈퇴")
    public ResponseEntity<?> deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.deleteUserById(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData("USER_200", "회원탈퇴 완료")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails){
        UserInfoDto userInfo = userService.getUserInfoByUserId(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success("USER_200", "회원정보 조회 성공", userInfo)
        );
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<?> updateMyNickname(@Valid @RequestBody UpdateNicknameRequestDto dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        userService.updateNickname(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData("USER_200", "닉네임 변경 성공")
        );
    }

    @PatchMapping("/me/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequestDto dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        userService.updatePassword(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(
          BaseResponse.successNoData("USER_200", "비밀번호 변경 성공")
        );
    }
}
