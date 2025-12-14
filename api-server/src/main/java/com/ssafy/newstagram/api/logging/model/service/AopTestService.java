package com.ssafy.newstagram.api.logging.model.service;

import com.ssafy.newstagram.api.logging.annotation.CollectLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AopTestService {

    @CollectLog //커스텀 어노테이션 실행
    public void testMethodForAop(Long articleId) {
        log.info("[AopTestService] - 가상의 테스트 비즈니스 로직 실행 중... (기사 ID: {})", articleId);
    }
}