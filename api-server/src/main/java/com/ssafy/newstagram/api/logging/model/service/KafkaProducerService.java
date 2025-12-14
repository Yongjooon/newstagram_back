package com.ssafy.newstagram.api.logging.model.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        /**
         * Aspect에서 sendMessage 호출 시 지정된 토픽에 메시지 전송
         * 콜백 함수 사용하여 비동기 처리
         */
        kafkaTemplate.send(topic, message).whenComplete((result, ex) -> {
            if(ex == null) log.info("[Kafka] API-Server Producer - 전송 성공 (topic: {})", topic);
            else log.error("[Kafka] API-Server Producer - 전송 실패 (서버: api-server, 토픽: {}, 메시지: {})", topic, message, ex);
        });
    }
}
