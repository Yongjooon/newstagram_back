package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.HotIssueSetDto;
import com.ssafy.newstagram.api.article.repository.PeriodRecommendationRepository;
import com.ssafy.newstagram.domain.util.period.Period;
import com.ssafy.newstagram.domain.util.period.PeriodCalculator;
import com.ssafy.newstagram.domain.recommend.entity.PeriodRecommendation;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HotIssueService {

    @Autowired
    private PeriodRecommendationRepository periodRecommendationRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Transactional
    public List<PeriodRecommendation> getPeriodRecommendations(String periodType,
                                                           LocalDateTime periodStart,
                                                           LocalDateTime periodEnd) {
        return periodRecommendationRepository.findByPeriodTypeAndPeriodStartAndPeriodEndOrderByRankingAsc(periodType, periodStart, periodEnd);
    }

    @Transactional
    public HotIssueSetDto getHotIssueSetDto(Period period) {
        PeriodCalculator calculator = period.getCalculator();
        LocalDateTime start = calculator.getStart();
        LocalDateTime end = calculator.getEnd();

        String periodKey = buildPeriodKey(period.name(), start);

        // 1. Redis 조회
        Object value = redisTemplate.opsForValue().get(periodKey);
        // cache hit
        if (value != null) return (HotIssueSetDto) value;

        // 2. db 조회
        List<PeriodRecommendation> list = getPeriodRecommendations(period.name(), start, end);

        // 3. dto 변환
        List<Long> ids = list.stream()
                .map(PeriodRecommendation::getId)
                .toList();

        HotIssueSetDto hotIssueSetDto = HotIssueSetDto.builder()
                .periodKey(periodKey)
                .periodType(period.name())
//                .periodStart(start)
//                .periodEnd(end)
                .items(ids)
                .build();

        redisTemplate.opsForValue().set(periodKey, hotIssueSetDto);

        return hotIssueSetDto;
    }

    public String buildPeriodKey(String periodName, LocalDateTime start) {
        return String.format(
                "HOT:%s:%s",
                periodName,
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"))
        );
    }

}
