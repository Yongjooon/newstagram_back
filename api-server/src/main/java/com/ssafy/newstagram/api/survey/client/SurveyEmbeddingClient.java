package com.ssafy.newstagram.api.survey.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.survey.model.dto.EmbeddingResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyEmbeddingClient {

    @Value("${gms.api.base-url}")
    private String gmsApiBaseUrl;

    @Value("${gms.api.key}")
    private String gmsApiKey;

    private static final String MODEL_NAME = "text-embedding-3-small";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 설문 카테고리 텍스트를 받아서 벡터로 변환
     */
    public List<Double> getEmbedding(String text) {
        List<List<Double>> results = callEmbeddingApiBatch(Collections.singletonList(text));

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("[SurveyEmbedding] - 임베딩 생성 실패: 결과가 없습니다.");
        }
        return results.get(0);
    }

    private List<List<Double>> callEmbeddingApiBatch(List<String> inputTexts) {
        String url = gmsApiBaseUrl.endsWith("/")
                ? gmsApiBaseUrl + "embeddings"
                : gmsApiBaseUrl + "/embeddings";

        try {
            String escapedInput = OBJECT_MAPPER.writeValueAsString(inputTexts);
            String rawJson = String.format(
                    "{\"model\":\"%s\",\"input\":%s}",
                    MODEL_NAME,
                    escapedInput
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(gmsApiKey);

            HttpEntity<String> entity = new HttpEntity<>(rawJson, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[SurveyEmbedding] - API 응답 실패 status={}", response.getStatusCode());
                throw new RuntimeException("[SurveyEmbedding] - Embedding API 실패");
            }

            EmbeddingResponse body = OBJECT_MAPPER.readValue(response.getBody(), EmbeddingResponse.class);

            if (body.getData() == null || body.getData().isEmpty()) {
                throw new RuntimeException("[SurveyEmbedding] - Embedding API data 필드 없음");
            }

            return body.getData().stream()
                    .sorted(Comparator.comparing(EmbeddingResponse.EmbeddingData::getIndex))
                    .map(EmbeddingResponse.EmbeddingData::getEmbedding)
                    .collect(Collectors.toList());

        } catch (HttpClientErrorException e) {
            log.error("[SurveyEmbedding] - GMS 4xx 에러: {}", e.getResponseBodyAsString());
            throw new RuntimeException("[SurveyEmbedding] - GMS 통신 에러", e);
        } catch (Exception e) {
            log.error("[SurveyEmbedding] - 처리 중 에러", e);
            throw new RuntimeException("[SurveyEmbedding] - 임베딩 처리 실패", e);
        }
    }

    @PostConstruct
    public void initLog() {
        log.info("[SurveyEmbeddingClient] - 초기화 완료. BaseUrl={}", gmsApiBaseUrl);
    }
}