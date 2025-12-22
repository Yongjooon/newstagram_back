package com.ssafy.newstagram.api.logging.controller;

import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.common.ErrorDetail;
import com.ssafy.newstagram.api.logging.model.dto.ArticleClickRequest;
import com.ssafy.newstagram.api.logging.model.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logging")
@RequiredArgsConstructor
@Tag(name = "Article Log API", description = "기사 클릭 및 사용자 활동 로그 수집 API")
public class LogApiController {

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
}