package com.ssafy.newstagram.rss.controller;

import com.ssafy.newstagram.rss.service.RssAndClusteringOrchestrator;
import com.ssafy.newstagram.rss.service.RssBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {

    private final RssBatchService rssBatchService;
    private final RssAndClusteringOrchestrator rssAndClusteringOrchestrator;

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @PostMapping("/rss")
    public Map<String, Object> startBatch() throws Exception {
        return rssBatchService.runRssMasterJob();
    }

    @PostMapping("/rsscluster")
    public Map<String, Object> startBatchCluster() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(SEOUL_ZONE);
        return rssAndClusteringOrchestrator.runFullPipeline(nowSeoul);
    }
}
