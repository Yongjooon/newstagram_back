package com.ssafy.newstagram.api.logging.model.service;

import com.ssafy.newstagram.api.logging.annotation.SurveyLog;
import com.ssafy.newstagram.api.logging.domain.User;
import com.ssafy.newstagram.api.logging.domain.repository.SurveyNewsCategoryRepository;
import com.ssafy.newstagram.api.logging.domain.repository.SurveyUserRepository;
import com.ssafy.newstagram.api.logging.model.dto.SurveyCategoryResponseDto;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {
    private final SurveyNewsCategoryRepository surveyNewsCategoryRepository;
    private final SurveyUserRepository surveyUserRepository;

    // 설문 페이지 항목들 불러오기
    public List<SurveyCategoryResponseDto> getSurveyCategories() {
        List<Long> excludedIds = Arrays.asList(1L, 17L);

        return surveyNewsCategoryRepository.findAllByIdNotIn(excludedIds).stream()
                .map(category -> SurveyCategoryResponseDto.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    // 사용자 임베딩 데이터 유무 조회
    public Map<?, ?> getEmbedding(CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        User user = surveyUserRepository.findById(userId).orElseThrow();

        boolean hasPreferenceEmbedding = user.getPreferenceEmbedding() != null;

        return Map.of("initialized", hasPreferenceEmbedding);
    }

    @SurveyLog
    public void captureSurveyLog(List<Long> categoryIds) {log.info("[Service] Kafka Log Survey Submit API - categoryIds={}", categoryIds);}
}