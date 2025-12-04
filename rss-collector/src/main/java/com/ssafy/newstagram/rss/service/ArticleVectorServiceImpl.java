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
    private static final String MODEL_NAME = "text-embedding-3-small";

    //그냥 Json 여기서
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

        for(Article article : targets){
            try{
                String inputText = buildEmbeddingInput(article);

                List<Double> embedding = callEmbeddingApi(inputText);
                if(embedding == null || embedding.isEmpty()){
                    log.warn("[Embedding] 임베딩 결과 없음, articleId={}", article.getId());
                    continue;
                }

                String embeddingLiteral = toPgVectorLiteral(embedding);
                int updated = articleMapper.updateEmbedding(article.getId(), embeddingLiteral);

                if(updated >0){
                    successCount++;
                    log.info("[Embedding] 저장 완료, articleId={}, dim={}", article.getId(), embedding.size());
                }
                else{
                    log.warn("[Embedding] DB에 저장 실패, articleId={}", article.getId());
                }
            }catch(GmsEmbeddingException e){
                hasGmsError = true;
                log.error("[Embedding] GMS 호출 에러, articleId={}, message={}", article.getId(), e.getMessage(),e);
            }catch(Exception e){
                log.error("[Embedding] 처리중 에러, articleId={}, message={}", article.getId(), e.getMessage(),e);
            }
        }
        log.info("[Embedding] sourceId={} 전체 완료, 전체 기사 수={}, 성공 개수={}", sourceId, targets.size(), successCount);
        return new VectorizeResult(sourceId, targets.size(), successCount, hasGmsError);
    }

    private String buildEmbeddingInput(Article article){
        StringBuilder sb = new StringBuilder();

        if(article.getTitle() !=null && !article.getTitle().isBlank()){
            sb.append(article.getTitle());
        }
        return sb.toString();
    }


    private List<Double> callEmbeddingApi(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Embedding input text must not be empty");
        }

        String url = gmsApiBaseUrl.endsWith("/")
                ? gmsApiBaseUrl + "embeddings"
                : gmsApiBaseUrl + "/embeddings";

        // 디버깅
        log.info("[Embedding] 실제 호출 URL={}", url);
        log.info("[Embedding] input length={}, preview='{}'",
                inputText.length(),
                inputText.length() > 50 ? inputText.substring(0, 50) + "..." : inputText);


        String escapedInput;
        try {
            escapedInput = OBJECT_MAPPER.writeValueAsString(inputText);
        } catch (Exception e) {
            throw new GmsEmbeddingException("입력 텍스트 JSON 직렬화 실패", e);
        }

        String rawJson = String.format(
                "{\"model\":\"%s\",\"input\":%s}",
                MODEL_NAME,
                escapedInput
        );

        log.info("[Embedding] RAW JSON={}", rawJson);

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
            log.error("[Embedding] GMS 4xx 에러. status={}, body={}", e.getStatusCode(), errBody);
            throw new GmsEmbeddingException("GMS/OpenAI 4xx 에러: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new GmsEmbeddingException("GMS 통신 실패", e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[Embedding] API 응답 실패, response={}", response.getStatusCode());
            throw new GmsEmbeddingException("Embedding API 실패, status=" + response.getStatusCode());
        }

        String bodyStr = response.getBody();
        if (bodyStr == null || bodyStr.isBlank()) {
            log.error("[Embedding] API 응답 body 없음");
            throw new GmsEmbeddingException("Embedding API 응답 비어있음");
        }

        EmbeddingResponse body;
        try {
            body = OBJECT_MAPPER.readValue(bodyStr, EmbeddingResponse.class);
        } catch (Exception e) {
            log.error("[Embedding] 응답 JSON 파싱 실패, body={}", bodyStr, e);
            throw new GmsEmbeddingException("Embedding API 응답 파싱 실패", e);
        }

        if (body.getData() == null || body.getData().isEmpty()) {
            log.error("[Embedding] API 응답 data 필드 없음, body={}", bodyStr);
            throw new GmsEmbeddingException("Embedding API data 필드 없음");
        }

        EmbeddingResponse.EmbeddingData first = body.getData().get(0);
        List<Double> embedding = first.getEmbedding();

        if (embedding == null || embedding.isEmpty()) {
            log.error("[Embedding] API 응답 embedding 필드 없음, body={}", bodyStr);
            throw new GmsEmbeddingException("Embedding API embedding 필드 없음");
        }

        return embedding;
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
