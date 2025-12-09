package com.ssafy.newstagram.api.exception;

import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.common.ErrorDetail;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("IllegalArgumentException 발생: {}", e.getMessage(), e);

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type("VALIDATION_ERROR")
                .message(e.getMessage())
                .build();

        return ResponseEntity
                .badRequest()
                .body(BaseResponse.error("USER_400", e.getMessage(), errorDetail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사 실패");

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type("VALIDATION_ERROR")
                .message(errorMessage)
                .build();

        return ResponseEntity
                .badRequest()
                .body(BaseResponse.error("USER_400", "유효성 검사 실패", errorDetail));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<BaseResponse<?>> handleJwtException(JwtException e){

        log.error("JWT error: {}", e.getMessage());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type("TOKEN_ERROR")
                .message("Invalid or expired token")
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.error("AUTH_401", "토큰 에러", errorDetail));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<?>> handleBadCredentialsException(BadCredentialsException e){

        log.error("Bad credentials error: {}", e.getMessage());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type("AUTHENTICATION_FAILED")
                .message("Invalid email or password")
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.error("AUTH_401", "이메일 또는 비밀번호가 올바르지 않습니다.", errorDetail));
    }

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
