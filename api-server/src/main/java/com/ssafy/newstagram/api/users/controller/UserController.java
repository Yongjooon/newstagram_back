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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
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
                    + "- 휴대폰번호, 이메일, 비밀번호, 닉네임 정보를 입력받아 회원을 생성합니다.\n"
                    + "- 회원가입 요청을 진행하기 전에, 휴대폰 인증을 먼저 진행하여야 합니다.\n"
                    + "- 휴대폰 인증 완료 여부는 1시간 동안 저장되므로, 인증 후 1시간 이내에 회원가입을 완료하여야 합니다.\n"
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

        // [User] 일반 회원가입 성공 로그 (비밀번호 제외, 이메일/닉네임 기록)
        log.info("[User] Email Register success: email={} nickname={}", dto.getEmail(), dto.getNickname());

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

        // [User] 회원탈퇴 로그 (요청한 사용자 ID 기록)
        log.info("[User] Delete account: userId={}", userDetails.getUserId());

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

        // [User] 내 정보 조회 로그
        log.info("[User] Get account info: userId={}", userDetails.getUserId());

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

        // [User] 닉네임 변경 로그 (어떤 유저가 어떤 닉네임으로 바꿨는지)
        log.info("[User] Update nickname: userId={} newNickname={}", userDetails.getUserId(), dto.getNewNickname());

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

        // [User] 비밀번호 변경 로그 (보안상 변경 값은 남기지 않고, 수행 사실과 유저 ID만 기록)
        log.info("[User] Update password: userId={}", userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(
          BaseResponse.successNoData("USER_200", "비밀번호 변경 성공")
        );
    }

    @PostMapping("/email/availability")
    @Operation(
            summary = "이메일 중복 체크",
            description = "회원가입 시 사용할 이메일의 사용 가능 여부를 확인합니다.\n\n" +
                    "- 사용 가능한 이메일인 경우 `available = true`가 반환됩니다.\n" +
                    "- 이미 사용 중인 이메일인 경우 `available = false`가 반환됩니다.\n" +
                    "- 탈퇴한 이메인인 경우, 현재 `available = false`가 반환됩니다.\n" +
                    "- 이 API는 중복 여부를 조회하는 용도로, 정상 요청 시에 항상 200 응답을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 중복 체크 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이메일 형식 오류 또는 잘못된 요청"
            )
    })
    public ResponseEntity<?> checkEmailAvailability(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이메일 중복 체크 요청 정보",
                    required = true
            )
            @Valid @RequestBody EmailAvailabilityRequestDto dto
    ){
        boolean isAvailable = userService.isAvailableEmail(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "USER_200",
                        "이메일 중복 체크 성공",
                        Map.of(
                                "available", isAvailable
                        )
                )
        );
    }

    @PostMapping("/phone-number/availability")
    @Operation(
            summary = "휴대폰 번호 중복 체크",
            description = "회원가입 시 사용할 휴대폰 번호의 사용 가능 여부를 확인합니다.\n\n" +
                    "- 사용 가능한 휴대폰 번호인 경우 `available = true`가 반환됩니다.\n" +
                    "- 이미 사용 중인 휴대폰 번호인 경우 `available = false`가 반환됩니다.\n" +
                    "- 탈퇴한 휴대폰 번호인 경우, 현재 `available = false`가 반환됩니다.\n" +
                    "- 이 API는 중복 여부를 조회하는 용도로, 정상 요청 시에 항상 200 응답을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "휴대폰 번호 중복 체크 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "휴대폰 번호 형식 오류 또는 잘못된 요청"
            )
    })
    public ResponseEntity<?> checkEmailAvailability(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "휴대폰 번호중복 체크 요청 정보",
                    required = true
            )
            @Valid @RequestBody PhoneNumberAvailabilityRequestDto dto
    ){
        boolean isAvailable = userService.isAvailablePhoneNumber(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "USER_200",
                        "휴대폰 번호 중복 체크 성공",
                        Map.of(
                                "available", isAvailable
                        )
                )
        );
    }

    @PostMapping("/nickname/availability")
    @Operation(
            summary = "닉네임 중복 체크",
            description = "닉네임의 중복 여부를 확인합니다.\n\n" +
                    "- 사용 가능한 닉네임인 경우 `available = true`가 반환됩니다.\n" +
                    "- 이미 사용 중인 닉네임인 경우 `available = false`가 반환됩니다.\n" +
                    "- 이 API는 중복 여부를 조회하는 용도로, 정상 요청 시에 항상 200 응답을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "닉네임 중복 체크 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "닉네임 형식 오류 또는 잘못된 요청"
            )
    })
    public ResponseEntity<?> checkNicknameAvailability(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "닉네임 중복 체크 요청 정보",
                    required = true
            )
            @Valid @RequestBody NicknameAvailabilityRequestDto dto
    ){
        boolean isAvailable = userService.isAvailableNickname(dto);
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "USER_200",
                        "닉네임 중복 체크 성공",
                        Map.of(
                                "available", isAvailable
                        )
                )
        );
    }
}
