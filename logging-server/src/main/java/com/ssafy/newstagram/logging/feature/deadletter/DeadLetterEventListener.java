package com.ssafy.newstagram.logging.feature.deadletter;

import com.ssafy.newstagram.domain.constant.KafkaTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
public class DeadLetterEventListener {
    @KafkaListener(topics = KafkaTopic.Log.INTERACTION_DLT, groupId = "dlt-test-group")
    public void listenDLT(String message) {
        log.error("[Kafka] DLT - 모든 재시도 실패. Dead Letter 수신: {}", message);
    }
}
