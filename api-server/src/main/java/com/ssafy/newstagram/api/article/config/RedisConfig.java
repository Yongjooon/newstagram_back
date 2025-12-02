package com.ssafy.newstagram.api.article.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableRedisRepositories
@Configuration
public class RedisConfig {

    // 다양한 자료구조와 객체 저장하기 위한 템플릿
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // JSON 직렬화/역직렬화를 위한 직렬화기 생성 - 자바 객체를 JSON으로 변환
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(new StringRedisSerializer());        // 키 직렬화
        redisTemplate.setValueSerializer(serializer);                       // 값 직렬화 (객체->JSON)
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());    // 해시 자료구조 키 직렬화
        redisTemplate.setHashValueSerializer(serializer);                   // 해시 자료구조 값 직렬화

        redisTemplate.afterPropertiesSet();     // 설정 후 초기화 작업
        return redisTemplate;
    }
}
