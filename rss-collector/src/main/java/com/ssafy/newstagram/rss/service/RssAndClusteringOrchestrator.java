package com.ssafy.newstagram.rss.service;

import com.ssafy.newstagram.rss.clustering.service.ClusteringOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssAndClusteringOrchestrator {
    private final RssBatchService rssBatchService;
    private final ClusteringOrchestratorService clusteringOrchestratorService;

    public Map<String, Object> runFullPipeline(ZonedDateTime firedAt) {
        Map<String, Object> result;

        try {
            log.info("[Pipeline] RSS + 임베딩 Master Job 시작");
            result = rssBatchService.runRssMasterJob();
            log.info("[Pipeline] RSS + 임베딩 Master Job 완료 - {}", result);
        } catch (Exception e) {
            log.error("[Pipeline] RSS + 임베딩 Master Job 실행 중 오류 발생 - 클러스터링 미실행", e);
            throw new RuntimeException("RSS Master Job failed", e);
        }

        int hour = firedAt.getHour();
        DayOfWeek dayOfWeek = firedAt.getDayOfWeek();

        log.info("[Pipeline] 클러스터링(REALTIME) 비동기 제출");
        clusteringOrchestratorService.runRealtime();

        if (hour == 0) {
            log.info("[Pipeline] 클러스터링(DAILY) 비동기 제출");
            clusteringOrchestratorService.runDaily();

            if (dayOfWeek == DayOfWeek.MONDAY) {
                log.info("[Pipeline] 클러스터링(WEEKLY) 비동기 제출");
                clusteringOrchestratorService.runWeekly();
            }
        }
        log.info("[Pipeline] 모든 클러스터링 병렬 작업 제출 완료");
        return result;
    }
}
