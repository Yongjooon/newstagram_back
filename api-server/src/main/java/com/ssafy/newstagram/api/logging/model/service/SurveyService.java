package com.ssafy.newstagram.api.logging.model.service;

import com.ssafy.newstagram.api.logging.annotation.SurveyLog;
import com.ssafy.newstagram.api.logging.domain.repository.SurveyNewsCategoryRepository;
import com.ssafy.newstagram.api.logging.model.dto.SurveyCategoryResponseDto;
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
    private final SurveyNewsCategoryRepository surveyNewsCategoryRepository;

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

    @SurveyLog
    public void captureSurveyLog(List<Long> categoryIds) {log.info("[LogService] - Survey Submit API (categoryIds : {})", categoryIds);}
}