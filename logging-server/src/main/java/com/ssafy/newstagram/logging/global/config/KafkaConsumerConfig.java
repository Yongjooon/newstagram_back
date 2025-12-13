package com.ssafy.newstagram.logging.global.config;

import com.ssafy.newstagram.domain.constant.KafkaTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    // 기사 클릭 관련 토픽
    @Bean
    public NewTopic clickLogTopic() {
        return TopicBuilder.name(KafkaTopic.Log.INTERACTION)
                .partitions(3)
                .replicas(3)
                .build();
    }

    // 기사 클릭 관련 토픽 DLT
    @Bean
    public NewTopic dltTopic() {
        return TopicBuilder.name(KafkaTopic.Log.INTERACTION_DLT)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
        long interval = 30000L; // 30초마다 재시도
        long maxAttempts = 5L; // 총 5회 반복
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,(r, e) -> new org.apache.kafka.common.TopicPartition(r.topic() + ".DLT", r.partition()));
        FixedBackOff backOff = new FixedBackOff(interval, maxAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("==================[Kafka] Consumer error / retry==================");
            log.warn("현재 시도 횟수 : 총 {}번째 중 {}번째 재시도 중 ({}초 마다 재시도)",maxAttempts, deliveryAttempt, interval);
            log.warn("실패한 메시지 : {}", record.value());
            log.warn("에러 원인 : {}", ex.getMessage());
        });

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
