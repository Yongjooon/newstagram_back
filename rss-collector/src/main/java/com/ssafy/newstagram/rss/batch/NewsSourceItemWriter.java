package com.ssafy.newstagram.rss.batch;


import com.ssafy.newstagram.rss.entity.NewsBatchLog;
import com.ssafy.newstagram.rss.entity.RssFeed;
import com.ssafy.newstagram.rss.repository.NewsBatchLogRepository;
import com.ssafy.newstagram.rss.repository.RssFeedRepository;
import com.ssafy.newstagram.rss.service.ArticleVectorService;
import com.ssafy.newstagram.rss.service.RssArticleService;
import com.ssafy.newstagram.rss.dto.ArticleCollectResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class NewsSourceItemWriter implements ItemWriter<Long> {

    private static final int MAX_RETRY = 2;

    private final RssArticleService rssArticleService;
    private final ArticleVectorService articleVectorService;
    private final NewsBatchLogRepository newsBatchLogRepository;
    private final RssFeedRepository rssFeedRepository;

    @Override
    public void write(Chunk<? extends Long> chunk) throws Exception{
        for(Long sourceId : chunk) {
            log.info("[Batch] 신문사 처리 시작, sourceId={}", sourceId);

            List<RssFeed> feeds = rssFeedRepository.findBySourceIdAndIsActiveTrue(sourceId);
            if(feeds.isEmpty()) {
                log.warn("[Batch] 활성화된 RSS 피드 없음, sourceId={}", sourceId);
                continue;
            }
            for(RssFeed feed : feeds) {
                processOneFeed(sourceId, feed);
            }
            log.info("[Batch] 신문사 처리 완료, sourceId={}", sourceId);
        }
    }

    private void processOneFeed(Long sourceId, RssFeed feed) {
        int retry = 0;
        boolean success = false;
        int insertedCount = 0;
        String lastErrorMessage = null;

        NewsBatchLog logEntity = NewsBatchLog.builder()
                .jobName("rssMasterJob")
                .runDate(LocalDateTime.now())
                .status("Running")
                .itemsProcessed(0)
                .retryCount(0)
                .feedId(feed.getId())
                .startedAt(LocalDateTime.now())
                .build();

        logEntity = newsBatchLogRepository.save(logEntity);

        while(retry <= MAX_RETRY && !success) {
            try{
                retry++;
                log.info(
                        "[Batch] feed 처리 시도, sourceId={}, feedId={}, url={}, retry={}",
                        sourceId, feed.getId(), feed.getRssUrl(), retry
                );

                //기사 수집
                ArticleCollectResultDto result = rssArticleService.collectArticlesByFeed(feed.getId());

                if(result.getErrors() != null && !result.getErrors().isEmpty()) {
                    lastErrorMessage = String.join(" | ", result.getErrors());

                    log.warn(
                            "[Batch] feed 처리 중 논리적 에러, sourceId={}, feedId={}, url={}, retry={}, errors={}",
                            sourceId, feed.getId(), feed.getRssUrl(), retry - 1, lastErrorMessage
                    );

                    if(retry > MAX_RETRY) {
                        break;
                    }
                    continue;
                }

                insertedCount = result.getInsertedCount();

                //백터화 수행
               // articleVectorService.vectorizeForSource(feed.getId());
                success = true;
            }catch(Exception e){
                lastErrorMessage = e.getMessage();
                log.warn(
                        "[Batch] feed 처리 에러, sourceId={}, feedId={}, url={}, retry={}",
                        sourceId, feed.getId(), feed.getRssUrl(), retry
                );
                if(retry > MAX_RETRY) {
                    break;
                }
            }
        }

        logEntity.setEndedAt(LocalDateTime.now());
        logEntity.setItemsProcessed(insertedCount);
        logEntity.setRetryCount(retry-1);

        if(success) {
            logEntity.setStatus("Success");
            logEntity.setMessage(
                    String.format("정상 수집 완료 (items=%d, retry=%d)", insertedCount, retry-1)
            );
            log.info(
                    "[Batch] feed 처리 성공, sourceId={}, feedId={}, items={}, retry={}",
                    sourceId, feed.getId(), insertedCount, retry-1
            );
        }
        else{
            logEntity.setStatus("Failed");
            logEntity.setMessage(
                    String.format("최대 재시도(%d) 초과, 마지막 에러: %s", MAX_RETRY, lastErrorMessage)
            );
            log.error(
                    "[Batch] feed 처리 실패(최대 재시도 초과), sourceId={}, feedId={}, lastError={}",
                    sourceId, feed.getId(), lastErrorMessage
            );
        }

        newsBatchLogRepository.save(logEntity);
    }
}
