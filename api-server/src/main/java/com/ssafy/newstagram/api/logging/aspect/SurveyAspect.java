package com.ssafy.newstagram.api.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.logging.model.dto.UserSurveySubmitDto;
import com.ssafy.newstagram.api.logging.model.service.KafkaProducerService;
import com.ssafy.newstagram.api.logging.util.GetUserId;
import com.ssafy.newstagram.domain.constant.KafkaTopic;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Aspect
@Component
public class SurveyAspect {
    private final KafkaProducerService producerService;
    private final ObjectMapper objectMapper;

    public SurveyAspect(KafkaProducerService producerService, ObjectMapper objectMapper) {
        this.producerService = producerService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.ssafy.newstagram.api.logging.annotation.SurveyLog)")
    public Object captureSurveySubmitLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Long userId = GetUserId.getUserIdFromSecurity();
        List<Long> categoryIds = findCategoryIdsFromArgs(joinPoint);
        if(userId == null || categoryIds.isEmpty()) {
            log.warn("[Kafka] ClickTrackingAspect 필수 데이터 누락 - UserId 또는 CategoryIds가 Null입니다.");
            return joinPoint.proceed();
        }

        String sessionId = (request.getSession(false) != null) ? request.getSession(false).getId() : "unknown";
        UserSurveySubmitDto submitDto = UserSurveySubmitDto.builder()
                .userId(userId)
                .categoryIds(categoryIds)
                .interactionType("CLICK")
                .sessionId(sessionId)
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(request.getRemoteAddr())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            String jsonMessage = objectMapper.writeValueAsString(submitDto);
            producerService.sendMessage(KafkaTopic.Log.SURVEY_SUBMIT, jsonMessage);
        } catch (Exception e) {
            log.error("[Kafka] 로그 전송 실패 - location={} userId={} categoryIds={}, errorMessage={}", methodName, userId, categoryIds, e.getMessage(), e);
        }

        return joinPoint.proceed();
    }

    private List<Long> findCategoryIdsFromArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || args[0] == null) return Collections.emptyList();;

        if (args[0] instanceof List) {
            return (List<Long>) args[0];
        }
        return Collections.emptyList();
    }
}
