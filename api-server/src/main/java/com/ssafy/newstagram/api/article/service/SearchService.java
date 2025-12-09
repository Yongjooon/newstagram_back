package com.ssafy.newstagram.api.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.dto.EmbeddingResponse;
import com.ssafy.newstagram.api.article.dto.IntentAnalysisResponse;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.api.article.repository.NewsCategoryRepository;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.api.users.repository.UserSearchHistoryRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import com.ssafy.newstagram.domain.user.entity.User;
import com.ssafy.newstagram.domain.user.entity.UserSearchHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ArticleRepository articleRepository;
    private final UserSearchHistoryRepository userSearchHistoryRepository;
    private final UserRepository userRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final ObjectProvider<SearchService> searchServiceProvider;
    
    @Value("${gms.api.base-url}")
    private String gmsApiBaseUrl;
    @Value("${gms.api.key}")
    private String gmsApiKey;
    @Value("${gms.api.llm-url}")
    private String gmsLlmUrl;
    
    private static final String MODEL_NAME = "text-embedding-3-small";
    private static final String LLM_MODEL_NAME = "gpt-5-nano";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int TIMEOUT_MS = 15000;

    @Transactional
    public List<ArticleDto> searchArticles(Long userId, String query, int limit) {
        // 1. Save Search History
        saveSearchHistory(userId, query);

        // 2. Perform Search (Cached)
        return getCachedSearchResults(query, limit);
    }

    @Cacheable(value = "search_results", key = "#query")
    public List<ArticleDto> getCachedSearchResults(String query, int limit) {
        IntentAnalysisResponse intent = searchServiceProvider.getObject().analyzeIntent(query);

        String searchKeywords = (intent.getQuery() != null && !intent.getQuery().isBlank()) 
                ? intent.getQuery() 
                : query;
        
        List<Double> embedding = callEmbeddingApi(searchKeywords);
        
        // Convert List<Double> to String format "[0.1,0.2,...]" for pgvector
        String embeddingString = toPgVectorLiteral(embedding); 

        Long categoryId = null;
        if (intent.getCategory() != null) {
            categoryId = newsCategoryRepository.findByName(intent.getCategory())
                    .map(NewsCategory::getId)
                    .orElse(null);
        }

        LocalDateTime startDate = null;
        if (intent.getDateRange() > 0) {
            startDate = LocalDateTime.now().minusDays(intent.getDateRange());
        }

        List<Article> articles = articleRepository.findByEmbeddingSimilarityWithFilters(
                embeddingString, limit, categoryId, startDate);

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "intent_analysis", key = "#userQuery", unless = "#result.query == #userQuery && #result.category == null")
    public IntentAnalysisResponse analyzeIntent(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return new IntentAnalysisResponse(userQuery, null, 0);
        }

        // Step 2: Hybrid Approach (Rule-based + LLM)
        // If query is short and simple (noun-based), skip LLM
        if (isSimpleQuery(userQuery)) {
            return new IntentAnalysisResponse(userQuery, null, 0);
        }

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
            "role", "system",
            "content", 
            "Analyze the user's search query and extract the intent. " +
            "Return ONLY a JSON object with keys: 'query' (refined keywords), " +
            "'category' (one of: TOP, POLITICS, ECONOMY, BUSINESS, SOCIETY, LOCAL, WORLD, NORTH_KOREA, CULTURE_LIFE, ENTERTAINMENT, SPORTS, WEATHER, SCIENCE_ENV, HEALTH, OPINION, PEOPLE, OTHER), " +
            "and 'date_range' (number of days, default 0). " +
            "Do not include markdown formatting like ```json."
        ));

        messages.add(Map.of(    
                "role", "user",
                "content", userQuery
            ));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", LLM_MODEL_NAME);
        requestBody.put("messages", messages);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            String requestBodyJson = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(gmsApiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(gmsLlmUrl, entity, Map.class);
        
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[Intent Analysis] API 응답 실패, response={}", response.getStatusCode());
                return new IntentAnalysisResponse(userQuery, null, 0);
            }

            if (response.getBody() != null) {
                Map body = response.getBody();
                List choices = (List) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    String content = (String) message.get("content");

                    content = content.replace("```json", "").replace("```", "").trim();

                    return OBJECT_MAPPER.readValue(content, IntentAnalysisResponse.class);
                } else {
                    log.error("[Intent Analysis] API 응답 body 없음 또는 choices 비어있음");
                }
            } else {
                log.error("[Intent Analysis] API 응답 body 없음");
            }
        } catch (HttpClientErrorException e) {
            log.error("[Intent Analysis] GMS 4xx 에러. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.warn("[Intent Analysis] Timeout or Network Error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[Intent Analysis] GMS 통신 실패 query={}", userQuery, e);
        }

        return new IntentAnalysisResponse(userQuery, null, 0);
    }

    private boolean isSimpleQuery(String query) {
        // 1. Check length (less than 5 words usually implies simple keyword search)
        if (query.split("\\s+").length < 5 && query.length() < 20) {
            // 2. Check for complex keywords that require LLM understanding
            return !containsComplexKeywords(query);
        }
        return false;
    }

    private boolean containsComplexKeywords(String query) {
        // Keywords that imply time, filtering, or complex intent
        String[] keywords = {
            // Time/Date
            "뉴스", "기사", "최근", "지난", "어제", "오늘", "내일", "이번", "올해", "작년", 
            "주간", "월간", "연간", "기간", "날짜", "언제", "동안", "이후", "이전",
            // Logic/Filtering
            "분류", "카테고리", "분야", "주제", "제외", "포함", "없이", "빼고", 
            // Sorting/Ranking/Action
            "추천", "인기", "순위", "정렬", "가장", "제일", "상위", "베스트", 
            "요약", "정리", "분석", "비교", "설명", "알려줘", "찾아줘", "보여줘",
            // Categories
            "정치", "경제", "사회", "문화", "세계", "국제", "스포츠", "연예", 
            "과학", "기술", "IT", "건강", "날씨", "오피니언", "사설"
        };
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String toPgVectorLiteral(List<Double> embedding){
        String inner = embedding.stream()
                .map(d -> String.format(java.util.Locale.US, "%.6f", d))
                .collect(Collectors.joining(","));
        return "[" + inner + "]";
    }

    private List<Double> callEmbeddingApi(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Embedding input text must not be empty");
        }

        String url = gmsApiBaseUrl.endsWith("/")
                ? gmsApiBaseUrl + "embeddings"
                : gmsApiBaseUrl + "/embeddings";

        String escapedInput;
        try {
            escapedInput = OBJECT_MAPPER.writeValueAsString(inputText);
        } catch (Exception e) {
            throw new RuntimeException("입력 텍스트 JSON 직렬화 실패", e);
        }

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
        ResponseEntity<EmbeddingResponse> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, EmbeddingResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("[Embedding] GMS 4xx 에러. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("GMS/OpenAI 4xx 에러: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new RuntimeException("GMS 통신 실패", e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[Embedding] API 응답 실패, response={}", response.getStatusCode());
            throw new RuntimeException("Embedding API 실패, status=" + response.getStatusCode());
        }

        EmbeddingResponse body = response.getBody();
        if (body == null || body.getData() == null || body.getData().isEmpty()) {
            log.error("[Embedding] API 응답 body 없음");
            throw new RuntimeException("Embedding API 응답 비어있음");
        }
        
        return body.getData().get(0).getEmbedding();
    }

    private void saveSearchHistory(Long userId, String query) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            UserSearchHistory history = UserSearchHistory.builder()
                    .user(user)
                    .query(query)
                    .build();

            userSearchHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Failed to save search history for user: {}", userId, e);
            // Do not fail the search if history saving fails
        }
    }

    private ArticleDto convertToDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .description(article.getDescription())
                .url(article.getUrl())
                .thumbnailUrl(article.getThumbnailUrl())
                .author(article.getAuthor())
                .build();
    }

    public List<String> getSearchHistory(Long userId) {
        return userSearchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(UserSearchHistory::getQuery)
                .distinct()
                .limit(10) // Limit to recent 10 unique queries
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSearchHistory(Long userId, String query) {
        userSearchHistoryRepository.deleteByUserIdAndQuery(userId, query);
    }

    @Transactional
    public void updateSearchHistory(Long userId, String oldQuery, String newQuery) {
        userSearchHistoryRepository.updateQueryByUserIdAndQuery(userId, oldQuery, newQuery);
    }
}
