package com.ssafy.newstagram.logging.feature.deadletter;

import com.ssafy.newstagram.domain.constant.KafkaTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
public class DeadLetterEventListener {
    @KafkaListener(topics = KafkaTopic.Log.INTERACTION_DLT, groupId = "dlt-test-group")
    public void listenArticleClickDLT(String message) {
        logErrorMsg(KafkaTopic.Log.INTERACTION_DLT, message);
    }

    @KafkaListener(topics = KafkaTopic.Log.SURVEY_SUBMIT_DLT, groupId = "dlt-test-group")
    public void listenSurveySubmitDLT(String message) {
        logErrorMsg(KafkaTopic.Log.SURVEY_SUBMIT_DLT, message);
    }

    public void logErrorMsg(String topic, String msg) {
        log.error("[Kafka] {} - 모든 재시도 실패. Dead Letter 수신: {}",topic, msg);
    }
}
