package com.ssafy.newstagram.logging.feature.surveysubmit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.domain.constant.KafkaTopic;
import com.ssafy.newstagram.logging.domain.Article;
import com.ssafy.newstagram.logging.domain.UserInteractionLog;
import com.ssafy.newstagram.logging.domain.repository.ArticleRepository;
import com.ssafy.newstagram.logging.domain.repository.UserInteractionLogRepository;
import com.ssafy.newstagram.logging.feature.articleclick.UserPreferenceService;
import com.ssafy.newstagram.logging.feature.surveysubmit.dto.SurveySubmitLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveySubmitConsumer {
    private final ObjectMapper objectMapper;
    private final ArticleRepository articleRepository;
    private final UserInteractionLogRepository logRepository;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopic.Log.SURVEY_SUBMIT, groupId = "logging-group")
    @Transactional
    public void SurveySubmitLog(String message) {
        try {
            SurveySubmitLogDto logDto = objectMapper.readValue(message, SurveySubmitLogDto.class);
            List<Long> categoryIds = logDto.getCategoryIds();
            List<Article> selectedArticles = articleRepository.findTop2ArticlesByCategoryIds(categoryIds);
            if(selectedArticles.isEmpty()) {
                log.warn("[Kafka] SurveySubmitConsumer - 조회된 기사가 없습니다. (userId: {})", logDto.getUserId());
                return;
            }

            List<UserInteractionLog> interactionLogs = selectedArticles.stream()
                    .map(article -> UserInteractionLog.builder()
                            .userId(logDto.getUserId())
                            .articleId(article.getId()) // 조회된 기사 ID 매핑
                            .interactionType(logDto.getInteractionType())
                            .sessionId(logDto.getSessionId())
                            .userAgent(logDto.getUserAgent())
                            .ipAddress(logDto.getIpAddress())
                            .createdAt(logDto.getCreatedAt())
                            .build())
                    .toList();
            logRepository.saveAll(interactionLogs);
            if (logDto.getUserId() != null) {
                userPreferenceService.updateUserPreference(logDto.getUserId());
            }
            log.info("[Kafka] SurveySubmitConsumer - UserId {}의 초기 인터랙션 로그 {}건 저장 완료", logDto.getUserId(), interactionLogs.size());
        } catch(Exception e) {
            log.error("[Kafka] SurveySubmitConsumer - 로그 소비 중 에러 발생 : {}", message, e);
        }
    }
}
