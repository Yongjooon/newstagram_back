package com.ssafy.newstagram.api.logging.model.service;

import com.ssafy.newstagram.api.logging.annotation.CollectLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {
    @CollectLog
    public void captureClickLog(Long articleId) {
        log.info("[LogService] - Article Click API (기사 ID: {})", articleId);
    }
}
