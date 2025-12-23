package com.ssafy.newstagram.rss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.rss.dto.EmbeddingResponse;
import com.ssafy.newstagram.rss.mapper.ArticleMapper;
import com.ssafy.newstagram.rss.vo.Article;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleVectorServiceImpl implements ArticleVectorService {
    private final ArticleMapper articleMapper;

    @Value("${gms.api.base-url}")
    private String gmsApiBaseUrl;
    @Value("${gms.api.key}")
    private String gmsApiKey;

    private static final String MODEL_NAME = "text-embedding-3-large";
    private static final int EMBEDDING_DIMENSIONS = 1536;

    // 본문이 너무 길어질 때 비용/토큰 폭증 방지
    private static final int MAX_CONTENT_CHARS = 6000;

    // 한번에 묶어 보낼 기사 개수
    //private static final int EMBEDDING_BATCH_SIZE = 128;
    private static final int EMBEDDING_BATCH_SIZE = 30;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public VectorizeResult vectorizeForSource(Long sourceId){
        List<Article> targets = articleMapper.findArticlesWithoutEmbeddingBySource(sourceId);
        if(targets == null || targets.isEmpty()){
            log.info("[Embedding] sourceId={} 에 임베딩 없는 기사 없음", sourceId);
            return new VectorizeResult(sourceId, 0, 0, false);
        }

        int successCount = 0;
        boolean hasGmsError = false;
        long startedAt = System.currentTimeMillis();

        for (int start = 0; start < targets.size(); start += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(start + EMBEDDING_BATCH_SIZE, targets.size());
            List<Article> batch = targets.subList(start, end);

            List<Article> validArticles = new ArrayList<>();
            List<String> inputs = new ArrayList<>();

            for (Article article : batch) {
                String inputText = buildEmbeddingInput(article);
                if (inputText == null || inputText.isBlank()) {
                    log.warn("[Embedding] 건너뜀(빈 제목), articleId={}", article.getId());
                    continue;
                }
                validArticles.add(article);
                inputs.add(inputText);
            }

            if (inputs.isEmpty()) {
                continue;
            }

            try {
                List<List<Double>> embeddings = callEmbeddingApiBatch(inputs);

                if (embeddings.size() != validArticles.size()) {
                    log.error("[Embedding] 응답 개수 불일치. request={}, response={}",
                            validArticles.size(), embeddings.size());
                    hasGmsError = true;
                    continue;
                }

                for (int i = 0; i < validArticles.size(); i++) {
                    Article article = validArticles.get(i);
                    List<Double> embedding = embeddings.get(i);

                    if (embedding == null || embedding.isEmpty()) {
                        log.warn("[Embedding] 임베딩 결과 없음, articleId={}", article.getId());
                        continue;
                    }

                    String embeddingLiteral = toPgVectorLiteral(embedding);
                    int updated = articleMapper.updateEmbedding(article.getId(), embeddingLiteral);

                    if(updated > 0){
                        successCount++;
                        log.info("[Embedding] 저장 완료, articleId={}, dim={}", article.getId(), embedding.size());
                    } else{
                        log.warn("[Embedding] DB에 저장 실패, articleId={}", article.getId());
                    }
                }
            } catch(GmsEmbeddingException e){
                hasGmsError = true;
                log.error("[Embedding] GMS 호출 에러 (batch), sourceId={}, message={}", sourceId, e.getMessage(), e);
            } catch(Exception e){
                log.error("[Embedding] 처리중 에러 (batch), sourceId={}, message={}", sourceId, e.getMessage(), e);
            }
        }

        long elapsed = System.currentTimeMillis() - startedAt;
        log.info("[Embedding] sourceId={} 전체 완료, 전체 기사 수={}, 성공 개수={}, elapsedMs={}",
                sourceId, targets.size(), successCount, elapsed);

        return new VectorizeResult(sourceId, targets.size(), successCount, hasGmsError);
    }

    // 기사 제목 정규화
    private String nomalizeTitle(String rawTitle){
        if (rawTitle == null){
            return "";
        }
        String normalized = rawTitle;

        // []태그 제거
        normalized = normalized.replaceAll("^\\s*\\[[^]]{1,15}\\]\\s*", "");
        normalized = normalized.replaceAll("\\s*\\[[^]]{1,15}\\]\\s*", " ");

        // () 제거
        normalized = normalized.replaceAll("\\s*\\([^)]{1,15}\\)\\s*$", "");

        // -~~기사 제거
        normalized = normalized.replaceAll("\\s*-\\s*[\\p{L}\\p{N}가-힣·\\s]{1,20}$", "");

        // 공백 제거
        normalized = normalized.replaceAll("\\s{2,}", " ").trim();

        return normalized;
    }

    /**
     * 변경 포인트:
     * - title(정규화) + content(원문) 결합해서 임베딩 입력 생성
     * - content가 비어있으면 title만 사용
     * - content 길이 제한 적용
     */
    private String buildEmbeddingInput(Article article){
        String title = safe(article.getTitle());
        if (title.isBlank()){
            return "";
        }

        String normalizedTitle = nomalizeTitle(title);
        if (!title.equals(normalizedTitle)) {
            log.debug("[Embedding] title 정규화: '{}' -> '{}'", title, normalizedTitle);
        }

        String content = safe(article.getContent());

        // content가 없으면 title만 임베딩
        if (content.isBlank()) {
            return normalizedTitle;
        }

        // content 길이 제한 (비용/토큰 방지)
        if (content.length() > MAX_CONTENT_CHARS) {
            content = content.substring(0, MAX_CONTENT_CHARS);
        }

        // 결합 텍스트
        return "TITLE: " + normalizedTitle + "\n\nCONTENT: " + content;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private List<Double> callEmbeddingApi(String inputText) {
        List<List<Double>> result = callEmbeddingApiBatch(List.of(inputText));
        return result.get(0);
    }

    private List<List<Double>> callEmbeddingApiBatch(List<String> inputTexts) {
        if (inputTexts == null || inputTexts.isEmpty()) {
            throw new IllegalArgumentException("Embedding input texts must not be empty");
        }

        String url = gmsApiBaseUrl.endsWith("/")
                ? gmsApiBaseUrl + "embeddings"
                : gmsApiBaseUrl + "/embeddings";

        int totalLength = inputTexts.stream().mapToInt(String::length).sum();
        log.info("[Embedding] BATCH 호출 URL={}, batchSize={}, totalCharLength={}",
                url, inputTexts.size(), totalLength);

        String escapedInput;
        try {
            escapedInput = OBJECT_MAPPER.writeValueAsString(inputTexts);
        } catch (Exception e) {
            throw new GmsEmbeddingException("입력 텍스트 JSON 직렬화 실패 (batch)", e);
        }

        String rawJson = String.format(
                "{\"model\":\"%s\",\"input\":%s,\"dimensions\":%d,\"encoding_format\":\"float\"}",
                MODEL_NAME,
                escapedInput,
                EMBEDDING_DIMENSIONS
        );

        log.info("[Embedding] BATCH RAW JSON={}", rawJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(gmsApiKey);

        HttpEntity<String> entity = new HttpEntity<>(rawJson, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            String errBody = e.getResponseBodyAsString();
            log.error("[Embedding] GMS 4xx 에러 (batch). status={}, body={}",
                    e.getStatusCode(), errBody);
            throw new GmsEmbeddingException("GMS/OpenAI 4xx 에러 (batch): " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new GmsEmbeddingException("GMS 통신 실패 (batch)", e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[Embedding] API 응답 실패 (batch), response={}", response.getStatusCode());
            throw new GmsEmbeddingException("Embedding API 실패 (batch), status=" + response.getStatusCode());
        }

        String bodyStr = response.getBody();
        if (bodyStr == null || bodyStr.isBlank()) {
            log.error("[Embedding] API 응답 body 없음 (batch)");
            throw new GmsEmbeddingException("Embedding API 응답 비어있음 (batch)");
        }

        EmbeddingResponse body;
        try {
            body = OBJECT_MAPPER.readValue(bodyStr, EmbeddingResponse.class);
        } catch (Exception e) {
            log.error("[Embedding] 응답 JSON 파싱 실패 (batch), body={}", bodyStr, e);
            throw new GmsEmbeddingException("Embedding API 응답 파싱 실패 (batch)", e);
        }

        if (body.getData() == null || body.getData().isEmpty()) {
            log.error("[Embedding] API 응답 data 필드 없음 (batch), body={}", bodyStr);
            throw new GmsEmbeddingException("Embedding API data 필드 없음 (batch)");
        }

        return body.getData().stream()
                .sorted(Comparator.comparing(EmbeddingResponse.EmbeddingData::getIndex))
                .map(EmbeddingResponse.EmbeddingData::getEmbedding)
                .collect(Collectors.toList());
    }

    private String toPgVectorLiteral(List<Double> embedding){
        String inner = embedding.stream()
                .map(d -> String.format(Locale.US, "%.6f", d))
                .collect(Collectors.joining(","));
        return "[" + inner + "]";
    }

    private static class GmsEmbeddingException extends RuntimeException{
        public GmsEmbeddingException(String message){
            super(message);
        }
        public GmsEmbeddingException(String message,Throwable cause){
            super(message,cause);
        }
    }

    @PostConstruct
    public void logGmsConfig() {
        String preview = gmsApiKey;
        if (preview != null && preview.length() > 12) {
            preview = preview.substring(0, 4) + "..." + preview.substring(preview.length() - 4);
        }

        log.info("[GMS] baseUrl='{}', keyEmpty={}, keyPreview='{}'",
                gmsApiBaseUrl,
                (gmsApiKey == null || gmsApiKey.isBlank()),
                preview);
    }
}
