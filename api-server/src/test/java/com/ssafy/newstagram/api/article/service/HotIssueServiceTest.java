package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.HotIssueSetDto;
import com.ssafy.newstagram.api.article.repository.PeriodRecommendationRepository;
import com.ssafy.newstagram.domain.util.period.Period;
import com.ssafy.newstagram.domain.util.period.PeriodCalculator;
import com.ssafy.newstagram.domain.recommend.entity.PeriodRecommendation;
import org.apache.juli.logging.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class HotIssueServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private PeriodRecommendationRepository periodRecommendationRepository;
    @Autowired
    private HotIssueService hotIssueService;

    @Test
    void testPeriodRecommendationRepo() {
        PeriodCalculator calculator = Period.DAILY.getCalculator();
        LocalDateTime start = calculator.getStart();
        LocalDateTime end = calculator.getEnd();
        System.out.println("테스트 "+start);
        System.out.println(end);

        List<PeriodRecommendation> list =
        periodRecommendationRepository.findByPeriodTypeAndPeriodStartAndPeriodEndOrderByRankingAsc("DAILY", start, end);
//        assertEquals(2L, list.get(0).getArticleId());
    }

    @Test
    void testGetHotIssueSetDto() {
        HotIssueSetDto hotIssueSetDto = hotIssueService.getHotIssueSetDto(Period.DAILY);
        PeriodCalculator calculator = Period.DAILY.getCalculator();
        LocalDateTime start = calculator.getStart();
        LocalDateTime end = calculator.getEnd();
//        assertEquals("HOT:DAILY:2025-11-27-00", hotIssueSetDto.getPeriodKey());
//        assertEquals(2L, hotIssueSetDto.getItems().get(0));
    }



}
