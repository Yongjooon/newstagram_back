package com.ssafy.newstagram.api.survey.model.service;

import com.ssafy.newstagram.api.survey.client.SurveyEmbeddingClient;
import com.ssafy.newstagram.api.survey.domain.User;
import com.ssafy.newstagram.api.survey.domain.repository.SurveyUserRepository;
import com.ssafy.newstagram.api.survey.model.dto.SurveyCategoryResponseDto;
import com.ssafy.newstagram.api.survey.domain.repository.SurveyNewsCategoryRepository;
import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {
    private final SurveyUserRepository surveyUserRepository;
    private final SurveyNewsCategoryRepository surveyNewsCategoryRepository;
    private final SurveyEmbeddingClient surveyEmbeddingClient;

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

    public void processSurvey(Long userId, List<Long> categoryIds) {
        // 1. 카테고리 조회
        List<NewsCategory> categories = surveyNewsCategoryRepository.findAllById(categoryIds);

        if (categories.isEmpty()) {
            throw new RuntimeException("선택된 카테고리가 없습니다.");
        }

        String textToEmbed = categories.stream()
                .map(NewsCategory::getDescription)
                .collect(Collectors.joining(" "));

        // 3. 임베딩 변환 (AI 모델 호출)
        List<Double> vector = surveyEmbeddingClient.getEmbedding(textToEmbed);

        User surveyUser = surveyUserRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("회원을 찾을 수 없습니다.")
        );

        surveyUser.setPreferenceEmbedding(vector);
        log.info("[SurveyService] - 사용자 {} 임베딩 업데이트 완료", userId);
    }
}
