package com.ssafy.newstagram.rss.clustering.service;

import com.ssafy.newstagram.rss.clustering.util.period.Period;
import com.ssafy.newstagram.rss.entity.NewsBatchLog;
import com.ssafy.newstagram.rss.repository.NewsBatchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClusteringOrchestratorService {
    private static final int MAX_RETRY = 2;

    private final ClusteringService clusteringService;
    private final NewsBatchLogRepository newsBatchLogRepository;

    private void runClusteringWithRetry(Period period) {
        String jobName = "clusteringJob-" + period.name();

        LocalDateTime startedAt = LocalDateTime.now();
        NewsBatchLog logEntity = NewsBatchLog.builder()
                .jobName(jobName)
                .runDate(startedAt)
                .status("Running")
                .itemsProcessed(0)
                .retryCount(0)
                .feedId(null)
                .startedAt(startedAt)
                .build();

        logEntity = newsBatchLogRepository.save(logEntity);

        boolean success = false;
        int attempt = 0;
        int retryCount = 0;
        String lastErrorMessage = null;

        while (attempt <= MAX_RETRY && !success) {
            try {
                attempt++;
                if (attempt > 1) {
                    retryCount++;
                }

                log.info(
                        "[Clustering] 실행 시도 - period={}, attempt={}, retryCount={}",
                        period, attempt, retryCount
                );

                clusteringService.clusteringByPeriodEnum(period);

                success = true;

            } catch (Exception ex) {
                lastErrorMessage = ex.getMessage();
                log.error(
                        "[Clustering] 실행 중 오류 - period={}, attempt={}, error={}",
                        period, attempt, ex.toString(), ex
                );
            }
        }

        logEntity.setEndedAt(LocalDateTime.now());
        logEntity.setRetryCount(retryCount);

        if (success) {
            logEntity.setStatus("Success");
            logEntity.setMessage(
                    String.format(
                            "클러스터링 성공 - period=%s, totalAttempts=%d, retryCount=%d",
                            period.name(), attempt, retryCount
                    )
            );

            log.info(
                    "[Clustering] 완료 - period={}, totalAttempts={}, retryCount={}",
                    period, attempt, retryCount
            );
        } else {
            logEntity.setStatus("Skipped");
            logEntity.setMessage(
                    String.format(
                            "클러스터링 실패 - 최대 재시도(%d) 초과, period=%s, lastError=%s",
                            MAX_RETRY, period.name(), lastErrorMessage
                    )
            );

            log.error(
                    "[Clustering] 실패(최대 재시도 초과, skip 처리) - period={}, lastError={}",
                    period, lastErrorMessage
            );
        }

        newsBatchLogRepository.save(logEntity);
    }

    @Async("clusteringExecutor")
    public CompletableFuture<Void> runRealtime() {
        runClusteringWithRetry(Period.REALTIME);
        return CompletableFuture.completedFuture(null);
    }

    @Async("clusteringExecutor")
    public CompletableFuture<Void> runDaily() {
        runClusteringWithRetry(Period.DAILY);
        return CompletableFuture.completedFuture(null);
    }

    @Async("clusteringExecutor")
    public CompletableFuture<Void> runWeekly() {
        runClusteringWithRetry(Period.WEEKLY);
        return CompletableFuture.completedFuture(null);
    }
}
