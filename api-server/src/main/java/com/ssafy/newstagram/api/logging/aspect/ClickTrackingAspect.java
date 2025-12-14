package com.ssafy.newstagram.api.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.logging.model.dto.UserInteractionLogsDto;
import com.ssafy.newstagram.api.logging.model.service.KafkaProducerService;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.domain.constant.KafkaTopic;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class ClickTrackingAspect {
    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;

    public ClickTrackingAspect(KafkaProducerService producerService, ObjectMapper objectMapper) {
        this.producerService = producerService;
        this.objectMapper = objectMapper;
    }

    /**
     * 뉴스 URL 클릭 시 호출
     * 로그 데이터 생성 및 Kafka 전송
     */
    @Around("@annotation(com.ssafy.newstagram.api.logging.annotation.CollectLog)")
    public Object captureArticleClickLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 메서드 정보 가져오기 (로깅용)
        String methodName = joinPoint.getSignature().toShortString();

        // 현재 요청에 대한 Request 정보 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // UserId 및 기사Id 추출
        Long articleId = findArticleIdFromArgs(joinPoint);
        Long userId = getUserIdFromSecurity();
        log.info("==== [Aspect Debug] ====");
        log.info("1. ArticleID 추출결과: {}", articleId);
        log.info("2. UserID 추출결과: {}", userId);
        log.info("========================");
        if (userId == null || articleId == null) {
            log.warn("[Kafka Skip] 필수 데이터 누락. UserId 또는 ArticleId가 Null입니다.");
            return joinPoint.proceed();
        }

        // 로그 DTO 빌드
        UserInteractionLogsDto logDto = UserInteractionLogsDto.builder()
                .userId(userId)
                .articleId(articleId)
                .interactionType("CLICK")
                .sessionId(request.getSession().getId())
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(request.getRemoteAddr())
                .createdAt(LocalDateTime.now())
                .build();

        // DTO -> JSON 변환 후 Kafka로 Message 전송
        try {
            String jsonMessage = objectMapper.writeValueAsString(logDto);
            producerService.sendMessage(KafkaTopic.Log.INTERACTION, jsonMessage);
        } catch(Exception e) {
            log.error("[Kafka Error] 로그 전송 실패 - 위치: {}, UserID: {}, ArticleID: {}, IP: {}, 에러메시지: {}", methodName, userId, articleId, request.getRemoteAddr(), e.getMessage(), e);
        }

        // 기존 메서드 실행
        return joinPoint.proceed();
    }

    /**
     * Spring Security Context에서 인증된 사용자 ID 추출
     */
    private Long getUserIdFromSecurity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }

            if (principal instanceof Long) {
                return (Long) principal;
            }

            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("[UserId Extraction] ID 파싱 실패 - 입력값: '{}' (Type: String)", principal);
                    return null;
                }
            }

        } catch (Exception e) {
            log.error("[UserId Extraction] 알 수 없는 에러 발생 - Authentication: {}", SecurityContextHolder.getContext().getAuthentication(), e);
        }

        return null;
    }

    /**
     * 기사 ID 인자값 추출
     */
    private Long findArticleIdFromArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) return null;

        if (args[0] instanceof Long) {
            return (Long) args[0];
        }
        if (args[0] instanceof Integer) {
            return ((Integer) args[0]).longValue();
        }
        return null;
    }
}
