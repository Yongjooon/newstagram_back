package com.ssafy.newstagram.rss.clustering.controller;

import com.ssafy.newstagram.domain.util.period.Period;
import com.ssafy.newstagram.domain.util.period.PeriodCalculator;
import com.ssafy.newstagram.rss.clustering.service.ClusteringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClusterController {

    private final ClusteringService clusteringService;

    // REALTIME, DAILY, WEEKLY
    @GetMapping("/clustering/{periodType}")
    public String clustering(@PathVariable("periodType") String periodType) {
        Period period = Period.valueOf(periodType);
        clusteringService.clusteringByPeriodEnum(period);
        return "클러스터링 완료";
    }

    @GetMapping("/clustering/test/period/{periodType}")
    public String getPeriod(@PathVariable("periodType") String periodType) {
        Period period = Period.valueOf(periodType);
        PeriodCalculator calculator = period.getCalculator();
        return "today: %s \nnowPeriod: %s ~ %s \nbeforePeriod: %s ~ %s"
                .formatted(LocalDateTime.now(), calculator.getStart(), calculator.getEnd(), calculator.getBeforeStart(), calculator.getBeforeEnd());
    }

}
