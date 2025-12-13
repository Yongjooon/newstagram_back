package com.ssafy.newstagram.api.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.dto.EmbeddingResponse;
import com.ssafy.newstagram.api.article.dto.IntentAnalysisResponse;
import com.ssafy.newstagram.api.article.dto.SearchHistoryDto;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.api.article.repository.NewsCategoryRepository;
import com.ssafy.newstagram.api.users.repository.UserRepository;
import com.ssafy.newstagram.api.users.repository.UserSearchHistoryRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import com.ssafy.newstagram.domain.user.entity.User;
import com.ssafy.newstagram.domain.user.entity.UserSearchHistory;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
@Transactional(readOnly = true)
public class SearchService {

    private final ArticleRepository articleRepository;
    private final UserSearchHistoryRepository userSearchHistoryRepository;
    private final UserRepository userRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    
    @Value("${gms.api.base-url}")
    private String gmsApiBaseUrl;
    @Value("${gms.api.key}")
    private String gmsApiKey;
    
    private static final String MODEL_NAME = "text-embedding-3-small";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
    private static final double INITIAL_DISTANCE_THRESHOLD = 0.30;
    private static final double MAX_DISTANCE_THRESHOLD = 0.80;
    private static final double STEP_DISTANCE_THRESHOLD = 0.05;

    @Transactional
    public List<ArticleDto> searchArticles(Long userId, String query, int limit) {
        // 1. Save Search History
        saveSearchHistory(userId, query);

        // 2. Perform Search (Cached)
        return getCachedSearchResults(query, limit);
    }

    @Cacheable(value = "search_results", key = "#query")
    public List<ArticleDto> getCachedSearchResults(String query, int limit) {
        log.info("[Search] Original Query: {}", query);

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

        String searchKeywords = (intent.getQuery() != null && !intent.getQuery().isBlank()) 
                ? intent.getQuery() 
                : query;
        
        List<Double> embedding = callEmbeddingApi(searchKeywords);
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

        // Optimization: Single DB Query with MAX threshold and large limit
        // Instead of looping DB queries, fetch a larger candidate pool once.
        // Since results are ordered by distance, the top results are the best matches.
        int fetchLimit = limit * 10; // Fetch plenty of candidates to handle filtering
        
        log.info("[Search] Searching once with threshold: {}, fetchLimit: {}", MAX_DISTANCE_THRESHOLD, fetchLimit);
        
        List<Article> candidates = articleRepository.findByEmbeddingSimilarityWithFilters(
                embeddingString, fetchLimit, categoryId, startDate, MAX_DISTANCE_THRESHOLD);

        List<Article> articles = new ArrayList<>();

        // Filter candidates based on keywords
        if (intent.getKeywords() != null && !intent.getKeywords().isEmpty()) {
            List<String> requiredKeywords = intent.getKeywords();
            for (Article candidate : candidates) {
                if (articles.size() >= limit) break;
                
                String title = candidate.getTitle() != null ? candidate.getTitle() : "";
                String content = candidate.getContent() != null ? candidate.getContent() : "";
                String textToCheck = title + " " + content;
                
                // Check if at least one keyword is present
                boolean match = requiredKeywords.stream()
                        .anyMatch(keyword -> textToCheck.contains(keyword));
                
                if (match) {
                    articles.add(candidate);
                }
            }
        } else {
            // No keywords to filter, just take top N
            articles = candidates.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private IntentAnalysisResponse analyzeIntentLocal(String query) {
        String category = null;
        int dateRange = 0;
        List<String> cleanKeywords = new ArrayList<>();
        String primaryCategoryKeyword = null; // 메인 카테고리 키워드 저장
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
                String matchedCat = matchCategory(morph);
                int matchedDate = matchDateRange(morph);
                
                // 1. 카테고리/날짜 키워드 처리 (임베딩에서 제외)
                if (matchedCat != null) {
                    flushChunk(cleanKeywords, currentChunk); // 이전 청크 저장
                    if (category == null) {
                        category = matchedCat;
                        primaryCategoryKeyword = morph;
                    }
                    continue;
                }
                
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

        // 2. Fallback: Simple Space Splitting (only if Komoran failed)
        if (!komoranSuccess) {
            String[] words = query.split("\\s+");
            for (String word : words) {
                String matchedCat = matchCategory(word);
                int matchedDate = matchDateRange(word);
                
                if (matchedCat != null) {
                    if (category == null) {
                        category = matchedCat;
                        primaryCategoryKeyword = word; // 키워드 저장
                    } else {
                        cleanKeywords.add(word);
                    }
                } else if (matchedDate != 0) {
                    if (dateRange == 0) dateRange = matchedDate;
                } else if (!isStopWord(word)) {
                    cleanKeywords.add(word);
                }
            }
        }
        
        // 3. 빈 쿼리 방지 로직: 모든 단어가 필터/불용어로 빠졌다면, 카테고리 키워드를 검색어로 복원
        if (cleanKeywords.isEmpty() && primaryCategoryKeyword != null) {
            cleanKeywords.add(primaryCategoryKeyword);
            log.info("[Search] Query is empty after filtering. Restored category keyword: '{}'", primaryCategoryKeyword);
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
        return "NNG".equals(pos) || "NNP".equals(pos) || "SL".equals(pos) || "SH".equals(pos) || "SN".equals(pos);
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
               word.equals("이나") || word.equals("부터") || word.equals("까지") || word.equals("필요");
    }    private String matchCategory(String word) {
        // 1. TOP (속보, 헤드라인)
        if (word.equals("속보") || word.equals("헤드라인") || word.equals("주요")) return "TOP";
        
        // 2. POLITICS (정치)
        if (word.equals("정치") || word.equals("국회") || word.equals("정당") || word.equals("선거") || 
            word.equals("청와대") || word.equals("대통령") || word.equals("행정")) return "POLITICS";
        
        // 3. ECONOMY (경제)
        if (word.equals("경제") || word.equals("금융") || word.equals("재테크")) return "ECONOMY";
        
        // 4. BUSINESS (기업, 산업)
        if (word.equals("기업") || word.equals("산업") || word.equals("증권") || word.equals("주식") || 
            word.equals("부동산") || word.equals("마켓") || word.equals("비즈니스")) return "BUSINESS";
        
        // 5. SOCIETY (사회)
        if (word.equals("사회") || word.equals("사건") || word.equals("사고") || word.equals("교육") || 
            word.equals("노동") || word.equals("인권")) return "SOCIETY";
        
        // 6. LOCAL (지역)
        if (word.equals("지역") || word.equals("전국") || word.equals("지방") || word.equals("광주") || 
            word.equals("부산") || word.equals("대구") || word.equals("대전") || word.equals("인천")) return "LOCAL";
        
        // 7. WORLD (국제)
        if (word.equals("세계") || word.equals("국제") || word.equals("해외") || word.equals("글로벌") || 
            word.equals("미국") || word.equals("중국") || word.equals("일본")) return "WORLD";
        
        // 8. NORTH_KOREA (북한)
        if (word.equals("북한") || word.equals("남북") || word.equals("통일")) return "NORTH_KOREA";
        
        // 9. CULTURE_LIFE (문화, 생활)
        if (word.equals("문화") || word.equals("생활") || word.equals("라이프") || word.equals("여행") || 
            word.equals("요리") || word.equals("책") || word.equals("공연") || word.equals("전시")) return "CULTURE_LIFE";
        
        // 10. ENTERTAINMENT (연예)
        if (word.equals("연예") || word.equals("예능") || word.equals("게임") || word.equals("영화") || 
            word.equals("드라마") || word.equals("스타") || word.equals("방송")) return "ENTERTAINMENT";
        
        // 11. SPORTS (스포츠)
        if (word.equals("스포츠") || word.equals("축구") || word.equals("야구") || word.equals("농구") || 
            word.equals("배구") || word.equals("골프") || word.equals("올림픽")) return "SPORTS";
        
        // 12. WEATHER (날씨)
        if (word.equals("날씨") || word.equals("기상") || word.equals("태풍") || word.equals("비") || 
            word.equals("눈") || word.equals("폭염") || word.equals("한파")) return "WEATHER";
        
        // 13. SCIENCE_ENV (과학, 환경)
        if (word.equals("과학") || word.equals("기술") || word.equals("환경") || word.equals("IT") || 
            word.equals("테크") || word.equals("모바일") || word.equals("인터넷") || word.equals("통신")) return "SCIENCE_ENV";
        
        // 14. HEALTH (건강)
        if (word.equals("건강") || word.equals("의료") || word.equals("병원") || word.equals("의학") || 
            word.equals("질병") || word.equals("코로나")) return "HEALTH";
        
        // 15. OPINION (사설)
        if (word.equals("사설") || word.equals("칼럼") || word.equals("오피니언") || word.equals("논설")) return "OPINION";
        
        // 16. PEOPLE (인물)
        if (word.equals("인물") || word.equals("사람") || word.equals("인터뷰") || word.equals("인사")) return "PEOPLE";
        
        return null;
    }

    private int matchDateRange(String word) {
        if (word.equals("오늘") || word.equals("금일") || word.equals("하루")) return 1;
        if (word.equals("어제") || word.equals("작일")) return 2;
        if (word.equals("이번주") || word.equals("주간") || word.equals("요즘") || word.equals("최근") || 
            word.equals("최신") || word.equals("일주일")) return 7;
        if (word.equals("이번달") || word.equals("월간") || word.equals("한달")) return 30;
        return 0;
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
                .publishedAt(article.getPublishedAt())
                .build();
    }

    public List<ArticleDto> getRecommendedArticles(Long userId) {
        // Use native query to fetch embedding as string to avoid Hibernate mapping issues
        String embeddingStr = userRepository.findPreferenceEmbeddingAsString(userId);
        
        if (embeddingStr == null || embeddingStr.isBlank()) {
            return new ArrayList<>();
        }

        // Fix time range to 7 days
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);

        // embeddingStr from Postgres cast(vector as varchar) is like "[0.1,0.2,...]"
        // We can pass this directly to the repository query
        List<Article> articles = articleRepository.findByEmbeddingSimilarityWithFilters(
                embeddingStr, 10, null, startDate, MAX_DISTANCE_THRESHOLD);

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SearchHistoryDto> getSearchHistory(Long userId) {
        return userSearchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(history -> new SearchHistoryDto(history.getId(), history.getQuery()))
                .limit(20)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSearchHistory(Long userId, Long historyId) {
        UserSearchHistory history = userSearchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("History not found"));

        if (!history.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to history");
        }

        userSearchHistoryRepository.delete(history);
    }

    @Transactional
    public void updateSearchHistory(Long userId, Long historyId, String newQuery) {
        UserSearchHistory history = userSearchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("History not found"));

        if (!history.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to history");
        }

        history.updateQuery(newQuery);
    }
}
