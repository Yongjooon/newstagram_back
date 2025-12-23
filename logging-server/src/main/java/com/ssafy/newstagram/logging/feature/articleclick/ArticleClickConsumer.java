package com.ssafy.newstagram.logging.feature.articleclick;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.logging.domain.repository.UserInteractionLogRepository;
import com.ssafy.newstagram.logging.feature.articleclick.dto.ArticleClickLogDto;
import com.ssafy.newstagram.domain.constant.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleClickConsumer {
    private final ObjectMapper objectMapper;
    private final UserInteractionLogRepository logRepository;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopic.Log.INTERACTION, groupId = "logging-group")
    public void ArticleClickLog(String message) {
        try {
            ArticleClickLogDto logDto = objectMapper.readValue(message, ArticleClickLogDto.class);
            logRepository.save(logDto.toEntity());
            if (logDto.getUserId() != null) {
                userPreferenceService.updateUserPreference(logDto.getUserId());
            }
        } catch(Exception e) {
            log.error("[Kafka] ArticleClickConsumer 로그 소비 중 에러 발생 - message={}", message, e);
        }
    }
}
