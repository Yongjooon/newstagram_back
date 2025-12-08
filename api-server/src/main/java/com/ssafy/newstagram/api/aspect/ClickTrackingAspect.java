package com.ssafy.newstagram.api.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.dto.UserInteractionLogsDto;
import com.ssafy.newstagram.api.service.KafkaProducerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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
    /**
     * Kafka 토픽 이름 정의
     * 규칙: [도메인].[유형].[행위] -> user.log.article.click
     */
    private static final String URL_CLICK_TOPIC_NAME = "user.log.article.click";

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
    @Around("@annotation(com.ssafy.newstagram.api.annotation.CollectLog)")
    public Object captureArticleClickLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 메서드 정보 가져오기 (로깅용)
        String methodName = joinPoint.getSignature().toShortString();

        // 현재 요청에 대한 Request 정보 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // UserId 및 기사Id 추출
        Long articledId = findArticleIdFromArgs(joinPoint);
        Long userId = getUserIdFromSecurity();

        // 로그 DTO 빌드
        UserInteractionLogsDto logDto = UserInteractionLogsDto.builder()
                .interactionType("CLICK")
                .createdAt(LocalDateTime.now())
                .sessionId(request.getSession().getId())
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(request.getRemoteAddr())
                .userId(userId)
                .articleId(articledId)
                .build();

        // DTO -> JSON 변환 후 Kafka로 Message 전송
        try {
            String jsonMessage = objectMapper.writeValueAsString(logDto);
            producerService.sendMessage(URL_CLICK_TOPIC_NAME, jsonMessage);
        } catch(Exception e) {
            log.error("[Kafka Error] 로그 전송 실패 - 위치: {}, UserID: {}, ArticleID: {}, IP: {}, 에러메시지: {}", methodName, userId, articledId, request.getRemoteAddr(), e.getMessage(), e);
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
        // 메서드 정보 가져오기 (에러 로그 기록용)
        String methodName = joinPoint.getSignature().toShortString();
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames == null || args == null) return null;

            for (int i = 0; i < paramNames.length; i++) {
                String name = paramNames[i].toLowerCase();

                if (name.contains("articleid")) {
                    Object value = args[i];
                    if (value == null) continue;

                    if (value instanceof Long) {
                        return (Long) value;
                    } else if (value instanceof Integer) {
                        return ((Integer) value).longValue();
                    } else if (value instanceof String) {
                        try {
                            return Long.parseLong((String) value);
                        } catch (NumberFormatException e) {
                            log.warn("[ArticleId Extraction] 숫자 변환 실패 - 메서드: {}, 파라미터명: {}, 입력값: '{}'", methodName, name, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[ArticleId Extraction] 파라미터 분석 중 시스템 에러 - 메서드: {}", methodName, e);
        }
        return null;
    }
}
