package com.ssafy.newstagram.api.users.controller;

import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.users.model.dto.*;
import com.ssafy.newstagram.api.users.model.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(
            summary = "일반 회원가입",
            description = "이메일 기반 일반 회원가입을 처리합니다.\n\n"
                    + "- 이메일, 비밀번호, 닉네임 정보를 입력받아 회원을 생성합니다.\n"
                    + "- 비밀번호는 서버에서 암호화되어 저장됩니다.\n"
                    + "- 동일한 이메일이 존재할 경우 회원가입이 실패합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공"
            )
    })
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 요청 정보",
                    required = true
            )
            @Valid @RequestBody RegisterRequestDto dto
    ) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.successNoData("USER_201", "회원가입 성공")
        );
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 사용자의 계정을 탈퇴(soft delete) 처리합니다.\n\n"
        + "- JWT 인증이 필요합니다.\n"
        + "- 계정은 즉시 삭제되지 않고 비활성화 상태로 전환됩니다.\n"
        + "- 일정 기간 후 관리자가 영구 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 성공"
            )
    })
    public ResponseEntity<?> deleteMyAccount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUserById(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData("USER_200", "회원 탈퇴 성공")
        );
    }

    @GetMapping("/me")
    @Operation(
            summary = "회원정보 조회",
            description = "로그인한 사용자의 회원 정보를 조회합니다.\n\n"
                    + "- JWT 인증이 필요합니다.\n"
                    + "- 토큰 정보 기반으로 본인 계정 정보만 조회됩니다.\n"
                    + "- 반환 정보에는 이메일, 닉네임, 권한 등의 기본 정보가 포함됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원정보 조회 성공"
            )
    })
    public ResponseEntity<?> getMyAccount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserInfoDto userInfo = userService.getUserInfoByUserId(userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success("USER_200", "회원정보 조회 성공", userInfo)
        );
    }

    @PatchMapping("/me/nickname")
    @Operation(
            summary = "닉네임 변경",
            description = "로그인한 사용자의 닉네임을 변경합니다.\n\n"
                    + "- JWT 인증이 필요합니다.\n"
                    + "- 본인 계정의 닉네임만 수정할 수 있습니다.\n"
                    + "- 닉네임은 유효성 검사를 통과해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 변경 성공"
            )
    })
    public ResponseEntity<?> updateMyNickname(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "닉네임 변경 요청 정보",
                    required = true
            )
            @Valid @RequestBody UpdateNicknameRequestDto dto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.updateNickname(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.successNoData("USER_200", "닉네임 변경 성공")
        );
    }

    @PatchMapping("/me/password")
    @Operation(
            summary = "비밀번호 변경",
            description = "로그인한 사용자의 비밀번호를 변경합니다.\n\n"
                    + "- JWT 인증이 필요합니다.\n"
                    + "- 현재 비밀번호를 검증한 후 새 비밀번호로 변경합니다.\n"
                    + "- 새로운 비밀번호는 유효성 검사를 통과해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공"
            )
    })
    public ResponseEntity<?> updatePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "비밀번호 요청 정보",
                    required = true
            )
            @Valid @RequestBody UpdatePasswordRequestDto dto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.updatePassword(userDetails.getUserId(), dto);
        return ResponseEntity.status(HttpStatus.OK).body(
          BaseResponse.successNoData("USER_200", "비밀번호 변경 성공")
        );
    }
}
