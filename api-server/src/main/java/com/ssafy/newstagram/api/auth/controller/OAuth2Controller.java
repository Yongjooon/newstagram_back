package com.ssafy.newstagram.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2Controller", description = "OAuth2를 이용한 인증/인가 기능 제공")
public class OAuth2Controller {

    @GetMapping("/authorization/google")
    @Operation(
            summary = "[에러 디버깅 중] Google OAuth 로그인 / 회원가입",
            description = """
                    Google 계정으로 로그인하기 위한 OAuth 인증 시작 엔드포인트입니다.
                    최초 로그인하는 경우, 임시 계정을 생성하고 휴대폰 인증을 요청합니다.

                    <br/><br/>
                    👉 <a href="/api/oauth2/authorization/google" target="_blank">
                    Google 로그인 페이지로 이동
                    </a>

                    <br/><br/>
                    - 클릭 시 새 탭에서 Google 로그인 화면으로 이동합니다.
                    - 이 엔드포인트는 JSON 응답을 반환하지 않고, OAuth 인증을 위해 리다이렉트됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "Google 로그인 페이지로 리다이렉트"
            )
    })
    public void googleOAuthLogin() {
        // ❗ 실제로 호출되지는 않음
        // Spring Security OAuth2가 이 경로를 가로챔
    }

}
