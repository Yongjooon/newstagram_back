package com.ssafy.newstagram.rss.batch;

import com.ssafy.newstagram.rss.clustering.service.ClusteringOrchestratorService;
import com.ssafy.newstagram.rss.service.RssBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class RssBatchQuartzJob implements Job {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final RssBatchService rssBatchService;
    private final ClusteringOrchestratorService clusteringOrchestratorService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        ZonedDateTime firedAt;
        if (context.getScheduledFireTime() != null) {
            firedAt = ZonedDateTime.ofInstant(
                    context.getScheduledFireTime().toInstant(), SEOUL_ZONE
            );
        } else if (context.getFireTime() != null) {
            firedAt = ZonedDateTime.ofInstant(
                    context.getFireTime().toInstant(), SEOUL_ZONE
            );
        } else {
            firedAt = ZonedDateTime.now(SEOUL_ZONE);
        }

        log.info("[Quartz] RSS 배치 Job 시작 - firedAt(Asia/Seoul)={}", firedAt);

        try {
            // RSS 수집 + 임베딩
            Map<String, Object> result = rssBatchService.runRssMasterJob();
            log.info("[Quartz] RSS 배치 완료 - result={}", result);

            // 클러스터링 병렬 실행
            int hour = firedAt.getHour();
            DayOfWeek dayOfWeek = firedAt.getDayOfWeek();

            // REALTIME
            log.info("[Quartz] 클러스터링(REALTIME) 비동기 제출");
            clusteringOrchestratorService.runRealtime();

            // DAILY
            if (hour == 0) {
                log.info("[Quartz] 클러스터링(DAILY) 비동기 제출");
                clusteringOrchestratorService.runDaily();

                // WEEKLY
                if (dayOfWeek == DayOfWeek.MONDAY) {
                    log.info("[Quartz] 클러스터링(WEEKLY) 비동기 제출");
                    clusteringOrchestratorService.runWeekly();
                }
            }
            log.info("[Quartz] 모든 클러스터링 Job 비동기 제출 완료");
        } catch (Exception e) {
            log.error("[Quartz] RSS 배치 실행 중 오류 발생", e);
            throw new JobExecutionException(e);
        }
    }
}
