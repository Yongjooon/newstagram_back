package com.ssafy.newstagram.api.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.dto.EmbeddingResponse;
import com.ssafy.newstagram.api.article.dto.IntentAnalysisResponse;
import com.ssafy.newstagram.api.article.dto.SearchHistoryDto;
import com.ssafy.newstagram.api.article.repository.NewsCategoryRepository;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.api.users.repository.UserSearchHistoryRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import com.ssafy.newstagram.domain.user.entity.User;
import com.ssafy.newstagram.domain.user.entity.UserSearchHistory;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleRepository articleRepository;
    private final UserSearchHistoryRepository userSearchHistoryRepository;
    private final UserRepository userRepository;
    private final NewsCategoryRepository newsCategoryRepository;

    @Autowired
    @Lazy
    private SearchService self;
    
    @Value("${gms.api.base-url}")
    private String gmsApiBaseUrl;
    @Value("${gms.api.llm-base-url}")
    private String gmsLlmBaseUrl;
    @Value("${gms.api.key}")
    private String gmsApiKey;
    
    private static final String MODEL_NAME = "text-embedding-3-small";
    private static final String LLM_MODEL_NAME = "gpt-4o-mini";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

    @Transactional(readOnly = true)
    public List<ArticleDto> searchArticles(Long userId, String query, int limit, int page) {
        // 1. Save Search History (Only for the first page)
        if (page == 0) {
            self.saveSearchHistory(userId, query);
        }

        // 2. Perform Search (Cached)
        // Authenticated search uses strict threshold (0.8) to limit results to relevant ones
        // Use 'self' to invoke via proxy for caching
        return self.getCachedSearchResults(query, limit, page, 0.80);
    }

    @Cacheable(value = "search_results", key = "#query + '-' + #page + '-' + #limit + '-' + #threshold")
    public List<ArticleDto> getCachedSearchResults(String query, int limit, int page, double threshold) {
        long totalStartTime = System.currentTimeMillis();
        log.info("[Search] Original Query: {}, Page: {}", query, page);

        // 1. Try Local Analysis (Rule-based)
        IntentAnalysisResponse intent = analyzeIntentLocal(query);

        if (intent == null) {
            intent = new IntentAnalysisResponse(query, null, 7, new ArrayList<>()); // Default 7 days if analysis fails
            log.info("[Search] Local Analysis Failed or Skipped. Using raw query with default 7 days.");
        } else {
            // Default to 7 days if no date specified
            if (intent.getDateRange() == 0) {
                intent.setDateRange(7);
            }
            log.info("[Search] Local Analysis Result: Query={}, Category={}, DateRange={}", 
                    intent.getQuery(), intent.getCategory(), intent.getDateRange());
        }

        // Case 2: Vector Search (Keywords exist OR Category not found)
        String searchKeywords = (intent.getQuery() != null && !intent.getQuery().isBlank()) 
                ? intent.getQuery() 
                : query;
        
        long embeddingStartTime = System.currentTimeMillis();
        List<Double> embedding = self.getCachedEmbedding(searchKeywords);
        long embeddingEndTime = System.currentTimeMillis();
        log.info("[Search] Embedding API took {} ms", (embeddingEndTime - embeddingStartTime));

        String embeddingString = toPgVectorLiteral(embedding); 

        // 3. LLM Category Analysis
        long llmStartTime = System.currentTimeMillis();
        List<Long> categoryIds = analyzeCategoryWithLLM(query);
        long llmEndTime = System.currentTimeMillis();
        log.info("[Search] LLM Category Analysis took {} ms", (llmEndTime - llmStartTime));

        LocalDateTime startDate = null;
        if (intent.getDateRange() > 0) {
            startDate = LocalDateTime.now().minusDays(intent.getDateRange());
        }

        // Optimization: Single DB Query with MAX threshold and large limit
        int candidateLimit = 800; 
        
        long dbStartTime = System.currentTimeMillis();
        List<Article> articles = articleRepository.findCandidatesByEmbedding(
                embeddingString, candidateLimit, categoryIds, startDate, threshold);
        long dbEndTime = System.currentTimeMillis();
        log.info("[Search] DB Query took {} ms", (dbEndTime - dbStartTime));

        List<String> filterKeywords = (intent.getKeywords() != null && !intent.getKeywords().isEmpty())
                ? intent.getKeywords()
                : List.of(query.split("\\s+"));

        List<ArticleDto> result = articles.stream()
                .filter(article -> {
                    String title = article.getTitle() != null ? article.getTitle() : "";
                    String description = article.getDescription() != null ? article.getDescription() : "";
                    // Check if ANY of the keywords are present
                    return filterKeywords.stream().anyMatch(keyword -> 
                        title.contains(keyword) || description.contains(keyword)
                    );
                })
                .sorted((a1, a2) -> a2.getPublishedAt().compareTo(a1.getPublishedAt())) // Sort by Date DESC
                .skip((long) page * limit) // Pagination in memory
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        long totalEndTime = System.currentTimeMillis();
        log.info("[Search] Total Service Execution took {} ms, query : {}", (totalEndTime - totalStartTime), query);

        return result;
    }

    private IntentAnalysisResponse analyzeIntentLocal(String query) {
        String category = null;
        int dateRange = 0;
        List<String> cleanKeywords = new ArrayList<>();
        boolean komoranSuccess = false;

        // 1. Komoran Analysis
        try {
            KomoranResult analyzeResultList = komoran.analyze(query);
            List<Token> tokenList = analyzeResultList.getTokenList();
            komoranSuccess = true;
            
            log.info("[Search] Komoran Tokens: {}", tokenList.stream()
                    .map(t -> t.getMorph() + "(" + t.getPos() + ")")
                    .collect(Collectors.joining(", ")));
            
            StringBuilder currentChunk = new StringBuilder();

            for (Token token : tokenList) {
                String morph = token.getMorph();
                String pos = token.getPos();
                int matchedDate = matchDateRange(morph);
                
                if (matchedDate != 0) {
                    flushChunk(cleanKeywords, currentChunk); // 이전 청크 저장
                    if (dateRange == 0) dateRange = matchedDate;
                    continue;
                }

                // 2. 불용어 처리 (임베딩에서 제외)
                if (isStopWord(morph)) {
                    flushChunk(cleanKeywords, currentChunk); // 이전 청크 저장
                    continue;
                }

                // 3. 검색어 병합 (Chunking)
                if (isSearchablePos(pos)) {
                    currentChunk.append(morph); // 공백 없이 병합 (예: 기아+타이거즈 -> 기아타이거즈)
                } else {
                    flushChunk(cleanKeywords, currentChunk); // 조사/어미 등 만나면 청크 종료
                }
            }
            flushChunk(cleanKeywords, currentChunk); // 남은 청크 저장

        } catch (Exception e) {
            log.error("[Search] Komoran Analysis Failed", e);
        }

        // 2. Fallback: Simple Space Splitting (only if Komoran failed OR resulted in empty keywords)
        // Komoran이 성공했더라도 모든 토큰이 필터링되어 키워드가 없는 경우(예: 신조어만 있거나 불용어만 있는 경우)
        // 단순 띄어쓰기 기준으로 다시 시도하여 불용어만 제거하고 나머지는 살린다.
        if (!komoranSuccess || cleanKeywords.isEmpty()) {
            cleanKeywords.clear(); // 혹시 모를 잔여 데이터 제거
            
            String[] words = query.split("\\s+");
            for (String word : words) {
                int matchedDate = matchDateRange(word);
                
                if (matchedDate != 0) {
                    if (dateRange == 0) dateRange = matchedDate;
                } else if (!isStopWord(word)) {
                    cleanKeywords.add(word);
                }
            }
        }
        
        String finalQuery = cleanKeywords.isEmpty() ? query : String.join(" ", cleanKeywords);
        if (finalQuery.isBlank()) finalQuery = query;
        
        return new IntentAnalysisResponse(finalQuery, category, dateRange, cleanKeywords);
    }

    private void flushChunk(List<String> keywords, StringBuilder chunk) {
        if (chunk.length() > 0) {
            String keyword = chunk.toString();
            keywords.add(keyword);
            log.debug("[Search] Chunk added: {}", keyword);
            chunk.setLength(0); // 버퍼 초기화
        }
    }

    private boolean isSearchablePos(String pos) {
        // NNG(일반명사) 포함: NNP(고유명사), SL(외국어), SH(한자), SN(숫자)
        // 연속된 명사를 병합하기 위해 NNG도 포함시킴 (단, 불용어 필터링 필수)
        // NA(분석불능), NF(추정명사), NV(추정동사), XR(어근) 추가하여 신조어/미등록어 대응
        return "NNG".equals(pos) || "NNP".equals(pos) || "SL".equals(pos) || "SH".equals(pos) || "SN".equals(pos) ||
               "NA".equals(pos) || "NF".equals(pos) || "NV".equals(pos) || "XR".equals(pos);
    }

    private boolean isStopWord(String word) {
        return word.equals("뉴스") || word.equals("기사") || word.equals("관련") || word.equals("소식") || 
               word.equals("내용") || word.equals("대해") || word.equals("관하") || word.equals("궁금") || 
               word.equals("알려") || word.equals("주") || word.equals("좀") || word.equals("어떻") || 
               word.equals("무엇") || word.equals("어") || word.equals("하") || word.equals("되") || 
               word.equals("이") || word.equals("가") || word.equals("을") || word.equals("를") || 
               word.equals("은") || word.equals("는") || word.equals("의") || word.equals("에") || 
               word.equals("에서") || word.equals("로") || word.equals("으로") || word.equals("와") || 
               word.equals("과") || word.equals("도") || word.equals("만") || word.equals("나") || 
               word.equals("이나") || word.equals("부터") || word.equals("까지") || word.equals("필요") ||
               word.equals("및") || word.equals("또는") || word.equals("혹은") || word.equals("그리고") ||
               word.equals("그러나") || word.equals("하지만") || word.equals("그래서") || word.equals("따라서") ||
               word.equals("때문에") || word.equals("인하여") || word.equals("위하") || word.equals("따르") ||
               word.equals("보이") || word.equals("보") || word.equals("드리") || word.equals("시키") ||
               word.equals("만들") || word.equals("가지") || word.equals("갖") || word.equals("그렇") ||
               word.equals("저렇") || word.equals("이렇") || word.equals("무슨") || word.equals("어느") ||
               word.equals("어떤") || word.equals("누구") || word.equals("언제") || word.equals("어디") ||
               word.equals("왜") || word.equals("어떻게") || word.equals("보도") || word.equals("속보") ||
               word.equals("결과") || word.equals("발표") || word.equals("예정") || word.equals("계획") ||
               word.equals("진행") || word.equals("상황") || word.equals("상태") || word.equals("문제") ||
               word.equals("해결") || word.equals("방안") || word.equals("대책") || word.equals("이유") ||
               word.equals("원인") || word.equals("배경") || word.equals("전망") || word.equals("분석") ||
               word.equals("평가") || word.equals("의견") || word.equals("주장") || word.equals("생각") ||
               word.equals("입장") || word.equals("반응") || word.equals("논란") || word.equals("의혹") ||
               word.equals("사실") || word.equals("확인") || word.equals("공개") || word.equals("등등") ||
               word.equals("것") || word.equals("수") || word.equals("등");
    }

    private int matchDateRange(String word) {
        if (word.equals("오늘") || word.equals("금일") || word.equals("하루")) return 1;
        if (word.equals("어제") || word.equals("작일")) return 2;
        if (word.equals("이번주") || word.equals("주간") || word.equals("요즘") || word.equals("최근") || 
            word.equals("최신") || word.equals("일주일")) return 7;
        if (word.equals("이번달") || word.equals("월간") || word.equals("한달")) return 30;
        if (word.equals("분기") || word.equals("3개월")) return 90;
        if (word.equals("상반기") || word.equals("하반기") || word.equals("반기")) return 180;
        if (word.equals("올해") || word.equals("연간") || word.equals("일년")) return 365;
        if (word.equals("작년") || word.equals("지난해")) return 730;
        return 0;
    }

    private String toPgVectorLiteral(List<Double> embedding){
        String inner = embedding.stream()
                .map(d -> String.format(java.util.Locale.US, "%.6f", d))
                .collect(Collectors.joining(","));
        return "[" + inner + "]";
    }

    @Cacheable(value = "keyword_embedding", key = "#inputText")
    public List<Double> getCachedEmbedding(String inputText) {
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

    private List<Long> analyzeCategoryWithLLM(String query) {
        log.info("[Prompt Search] query={}", query);
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        String prompt = "Analyze the following search query and identify at least the top 3 most relevant categories from the list below. " +
                "Return ONLY the category codes separated by commas (e.g., POLITICS, ECONOMY, WORLD). " +
                "Categories:\n" +
                "TOP (속보, 최신 기사, 헤드라인, 전체 기사 스트림)\n" +
                "POLITICS (정치 관련 기사)\n" +
                "ECONOMY (경제 관련 기사)\n" +
                "BUSINESS (기업, 산업, 증권, 부동산, 마켓 관련 기사)\n" +
                "SOCIETY (사회 일반 기사)\n" +
                "LOCAL (지역, 전국 이슈)\n" +
                "WORLD (국제, 세계 뉴스)\n" +
                "NORTH_KOREA (북한 관련 기사)\n" +
                "CULTURE_LIFE (문화, 생활, 라이프 기사)\n" +
                "ENTERTAINMENT (연예, 예능, 게임 등)\n" +
                "SPORTS (스포츠 기사)\n" +
                "WEATHER (날씨 기사)\n" +
                "SCIENCE_ENV (과학, 기술, 환경 기사)\n" +
                "HEALTH (건강, 의료 기사)\n" +
                "OPINION (사설, 칼럼, 오피니언)\n" +
                "PEOPLE (사람들, 인물 기사)\n" +
                "Query: " + query;

        String url = gmsApiBaseUrl.endsWith("/")
                ? gmsApiBaseUrl + "chat/completions"
                : gmsApiBaseUrl + "/chat/completions";

        try {
            String requestBody = OBJECT_MAPPER.writeValueAsString(java.util.Map.of(
                    "model", LLM_MODEL_NAME,
                    "messages", List.of(
                            java.util.Map.of("role", "developer", "content", "You are a helpful assistant that categorizes news queries."),
                            java.util.Map.of("role", "user", "content", prompt)
                    )
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(gmsApiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            
            ResponseEntity<com.fasterxml.jackson.databind.JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, com.fasterxml.jackson.databind.JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String content = response.getBody().path("choices").get(0).path("message").path("content").asText();
                log.info("[Search] LLM Response: {}", content);
                
                List<Long> categoryIds = new ArrayList<>();
                String[] codes = content.split(",");
                for (String code : codes) {
                    String cleanCode = code.trim().toUpperCase();
                    if (cleanCode.contains(" ")) {
                        cleanCode = cleanCode.split("\\s+")[0];
                    }
                    
                    String finalCode = cleanCode;
                    newsCategoryRepository.findByName(finalCode).ifPresent(category -> categoryIds.add(category.getId()));
                }
                return categoryIds;
            }
        } catch (Exception e) {
            log.error("[Search] LLM Category Analysis Failed", e);
        }
        
        return new ArrayList<>();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearchHistory(Long userId, String query) {
        try {
            // 1. Try to update existing history timestamp to move it to top
            int updated = userSearchHistoryRepository.updateCreatedAtByUserIdAndQuery(userId, query);

            // 2. If not exists, save new history
            if (updated == 0) {
                User user = userRepository.getReferenceById(userId);
                UserSearchHistory history = UserSearchHistory.builder()
                        .user(user)
                        .query(query)
                        .build();

                userSearchHistoryRepository.save(history);
            }
        } catch (Exception e) {
            log.error("[Serach History] Failed to save search history for user={}", userId, e);
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
                .publishedAt(article.getPublishedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getRecommendedArticles(Long userId, int page, int limit) {
        // Use native query to fetch embedding as string to avoid Hibernate mapping issues
        String embeddingStr = userRepository.findPreferenceEmbeddingAsString(userId);
        
        if (embeddingStr == null || embeddingStr.isBlank()) {
            return new ArrayList<>();
        }

        // Fix time range to 7 days
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        int offset = page * limit;

        // Use a relaxed threshold (2.0) for recommendations to allow infinite scrolling
        List<Article> articles = articleRepository.findByEmbeddingSimilarityWithFilters(
                embeddingStr, limit, offset, null, startDate, 2.0);

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryDto> getSearchHistory(Long userId) {
        return userSearchHistoryRepository.findHistoryNative(userId).stream()
                .map(history -> new SearchHistoryDto(history.getId(), history.getQuery()))
                .limit(20)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSearchHistory(Long userId, Long historyId) {
        int deletedCount = userSearchHistoryRepository.deleteByIdAndUserId(historyId, userId);
        if (deletedCount == 0) {
            throw new IllegalArgumentException("History not found or unauthorized");
        }
    }

    @Transactional
    public void updateSearchHistory(Long userId, Long historyId, String newQuery) {
        int updatedCount = userSearchHistoryRepository.updateQueryByIdAndUserId(historyId, userId, newQuery);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("History not found or unauthorized");
        }
    }
}
