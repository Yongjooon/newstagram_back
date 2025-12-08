package com.ssafy.newstagram.rss.batch;

import com.ssafy.newstagram.rss.entity.NewsBatchLog;
import com.ssafy.newstagram.rss.entity.RssFeed;
import com.ssafy.newstagram.rss.repository.NewsBatchLogRepository;
import com.ssafy.newstagram.rss.repository.RssFeedRepository;
import com.ssafy.newstagram.rss.service.ArticleVectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ArticleEmbeddingItemWriter implements ItemWriter<Long> {

    private static final int MAX_RETRY = 2;

    private final ArticleVectorService articleVectorService;
    private final NewsBatchLogRepository newsBatchLogRepository;
    private final RssFeedRepository rssFeedRepository;

    @Override
    public void write(Chunk<? extends Long> chunk) throws Exception {
        for (Long sourceId : chunk) {
            if (sourceId == null) {
                continue;
            }
            processOneSource(sourceId);
        }
    }


    private void processOneSource(Long sourceId) {
        int retry = 0;
        boolean success = false;
        int embeddedCount = 0;
        String lastErrorMessage = null;

        List<RssFeed> feeds = rssFeedRepository.findBySourceIdAndIsActiveTrue(sourceId);
        if (feeds == null || feeds.isEmpty()) {
            log.warn("[EmbeddingStep] 활성 RSS 피드가 없는 sourceId={}, 로그는 남기지 않고 임베딩 시도만 진행합니다.", sourceId);
        }

        Long feedIdForLog = (feeds == null || feeds.isEmpty()) ? null : feeds.get(0).getId();

        NewsBatchLog logEntity = NewsBatchLog.builder()
                .jobName("rssEmbeddingJob")
                .runDate(LocalDateTime.now())
                .status("Running")
                .itemsProcessed(0)
                .retryCount(0)
                .feedId(feedIdForLog)
                .startedAt(LocalDateTime.now())
                .build();

        if (feedIdForLog != null) {
            logEntity = newsBatchLogRepository.save(logEntity);
        }

        while (retry <= MAX_RETRY && !success) {
            try {
                retry++;
                log.info("[EmbeddingStep] 임베딩 시도 - sourceId={}, retry={}", sourceId, retry);

                ArticleVectorService.VectorizeResult result =
                        articleVectorService.vectorizeForSource(sourceId);

                embeddedCount = result.getSuccessCount();

                if ("error".equalsIgnoreCase(result.getStatus())) {
                    lastErrorMessage = String.format(
                            "벡터화 오류 (status=%s, total=%d, success=%d, hasGmsError=%s)",
                            result.getStatus(),
                            result.getTotalCount(),
                            result.getSuccessCount(),
                            result.isHasGmsError()
                    );

                    log.warn(
                            "[EmbeddingStep] 임베딩 결과 에러 - sourceId={}, retry={}, message={}",
                            sourceId, retry, lastErrorMessage
                    );

                    if (retry > MAX_RETRY) {
                        break;
                    }
                    continue;
                }

                success = true;

            } catch (Exception e) {
                lastErrorMessage = e.getMessage();
                log.warn(
                        "[EmbeddingStep] 임베딩 중 예외 발생 - sourceId={}, retry={}, error={}",
                        sourceId, retry, lastErrorMessage, e
                );

                if (retry > MAX_RETRY) {
                    break;
                }
            }
        }

        if (feedIdForLog == null) {
            if (success) {
                log.info(
                        "[EmbeddingStep] 로그 없이 임베딩 성공 - sourceId={}, embeddedCount={}, retryCount={}",
                        sourceId, embeddedCount, Math.max(0, retry - 1)
                );
            } else {
                log.error(
                        "[EmbeddingStep] 로그 없이 임베딩 실패 (최대 재시도 초과) - sourceId={}, lastError={}",
                        sourceId, lastErrorMessage
                );
            }
            return;
        }

        int retryCount = Math.max(0, retry - 1);

        logEntity.setRetryCount(retryCount);
        logEntity.setItemsProcessed(embeddedCount);
        logEntity.setEndedAt(LocalDateTime.now());

        if (success) {
            logEntity.setStatus("Success");
            logEntity.setMessage(
                    String.format(
                            "임베딩 정상 완료 (sourceId=%d, items=%d, retryCount=%d)",
                            sourceId, embeddedCount, retryCount
                    )
            );

            log.info(
                    "[EmbeddingStep] 임베딩 완료 - sourceId={}, embeddedCount={}, retryCount={}",
                    sourceId, embeddedCount, retryCount
            );
        } else {
            logEntity.setStatus("Skipped");
            logEntity.setMessage(
                    String.format(
                            "임베딩 실패 - 최대 재시도(%d) 초과, sourceId=%d, lastError=%s",
                            MAX_RETRY, sourceId, lastErrorMessage
                    )
            );

            log.error(
                    "[EmbeddingStep] 임베딩 실패(최대 재시도 초과, skip 처리) - sourceId={}, lastError={}",
                    sourceId, lastErrorMessage
            );
        }

        newsBatchLogRepository.save(logEntity);
    }
}
