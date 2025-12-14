package com.ssafy.newstagram.api.logging.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.common.ErrorDetail;
import com.ssafy.newstagram.api.logging.model.dto.ArticleClickRequest;
import com.ssafy.newstagram.api.logging.model.service.AopTestService;
import com.ssafy.newstagram.api.logging.model.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/logging")
@RequiredArgsConstructor
@Tag(name = "Article Log API", description = "기사 클릭 및 사용자 활동 로그 수집 API")
public class LogApiController {

    private final AopTestService aopTestService;
    private final LogService logService;

    @PostMapping("/click")
    @Operation(
            summary = "기사 클릭 이벤트 수집 (Cookie)",
            description = "사용자가 기사를 클릭했을 때 발생하는 이벤트를 수집합니다.\n"
                    + "쿠키에서 사용자 정보를 식별하여 Kafka로 클릭 로그를 발행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그 수집 요청 성공"),
            @ApiResponse(responseCode = "400", description = "articleId 누락"),
    })
    public ResponseEntity<?> logArticleClick(@RequestBody ArticleClickRequest request) {
        if (request.getArticleId() == null) {
            ErrorDetail errorDetail = ErrorDetail.builder()
                    .type("INVALID_PARAMETER")
                    .message("Article ID는 필수 값입니다.")
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    BaseResponse.error(
                            "LOG_400",
                            "잘못된 요청입니다.",
                            errorDetail
                    )
            );
        }

        logService.captureClickLog(request.getArticleId());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "LOG_200",
                        "로그 수집 요청 성공",
                        "Log Processing started"
                )
        );
    }

    @PostMapping("/test/click")
    @Operation(
            summary = "[TEST] 기사 클릭 이벤트 시뮬레이션 (UserID 지정)",
            description = "테스트를 위해 특정 UserID를 강제로 주입하여 기사 클릭 로그를 발행합니다.\n"
                    + "실제 로그인 과정 없이 로그 적재 로직을 검증할 때 사용합니다."
    )
    public String sendTestLog(
            @Parameter(description = "가상 유저 ID", example = "2") @RequestParam Long userId,
            @Parameter(description = "클릭한 기사 ID", example = "55") @RequestParam Long articleId
    ) throws JsonProcessingException {
        UsernamePasswordAuthenticationToken fakeAuth =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

        // SecurityContext에 테스트 정보 주입
        SecurityContextHolder.getContext().setAuthentication(fakeAuth);

        try {
            aopTestService.testMethodForAop(articleId);
            return "Aspect 테스트 성공! (SecurityContext에 유저 " + userId + " 주입 완료)";
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}