package com.ssafy.newstagram.api.exception;

import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.common.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 모든 예외를 잡는 가장 상위 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
        log.error("서버 내부 에러 발생: {}", e.getMessage(), e);

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type("INTERNAL_SERVER_ERROR")
                .message("서버 오류가 발생했습니다.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error("SYSTEM_500", "서버 내부 오류입니다.", errorDetail));
    }
}
