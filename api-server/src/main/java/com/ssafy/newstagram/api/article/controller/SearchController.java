package com.ssafy.newstagram.api.article.controller;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.dto.SearchHistoryDto;
import com.ssafy.newstagram.api.article.dto.SearchRequest;
import com.ssafy.newstagram.api.article.dto.UpdateSearchHistoryRequest;
import com.ssafy.newstagram.api.article.service.SearchService;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Search", description = "시맨틱 검색 API")
@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "시맨틱 검색", description = "형태소 분석과 LLM 카테고리 분석 및 벡터 유사도 기반의 하이브리드 방식을 사용하여 기사를 검색합니다.")
    @PostMapping
    public ResponseEntity<List<ArticleDto>> searchArticles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SearchRequest request) {
        
        String query = request.getQuery();
        Integer limit = request.getLimit();
        Integer page = request.getPage();
        
        if (limit == null) limit = 10;
        if (page == null) page = 0;

        Long userId = userDetails.getUserId();

        long startTime = System.currentTimeMillis();
        
        List<ArticleDto> results = searchService.searchArticles(userId, query, limit, page);
        
        long endTime = System.currentTimeMillis();

        log.info("[SearchController] Slow Request Check - Time: {}ms | Query: {}, Limit: {}, Page: {}", 
        (endTime - startTime), query, limit, page);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "시맨틱 검색 (테스트)", description = "사용자 인증 없이 형태소 분석과 LLM 카테고리 분석 및 벡터 유사도 기반의 하이브리드 방식을 사용하여 기사를 검색합니다.")
    @PostMapping("/test")
    public ResponseEntity<List<ArticleDto>> searchArticlesTest(@RequestBody SearchRequest request) {
        
        String query = request.getQuery();
        Integer limit = request.getLimit();
        Integer page = request.getPage();
        
        if (limit == null) limit = 10;
        if (page == null) page = 0;

        // Skip saving history and directly search
        // Test search uses strict threshold (0.8) to limit results
        List<ArticleDto> results = searchService.getCachedSearchResults(query, limit, page, 0.80);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "검색 기록 조회", description = "사용자의 최근 검색 기록을 조회합니다.")
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistoryDto>> getSearchHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        List<SearchHistoryDto> history = searchService.getSearchHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "검색 기록 삭제", description = "특정 검색 기록을 삭제합니다.")
    @DeleteMapping("/history")
    public ResponseEntity<Void> deleteSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long historyId) {
        Long userId = userDetails.getUserId();
        searchService.deleteSearchHistory(userId, historyId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "검색 기록 수정", description = "특정 검색 기록의 검색어를 수정합니다.")
    @PutMapping("/history")
    public ResponseEntity<Void> updateSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateSearchHistoryRequest request) {
        Long userId = userDetails.getUserId();
        Long historyId = request.getHistoryId();
        String newQuery = request.getNewQuery();
        
        if (historyId == null || newQuery == null) {
            return ResponseEntity.badRequest().build();
        }

        searchService.updateSearchHistory(userId, historyId, newQuery);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "추천 기사 조회", description = "사용자 선호도를 기반으로 추천 기사를 조회합니다.")
    @GetMapping("/preference")
    public ResponseEntity<List<ArticleDto>> getRecommendedArticles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int limit) {
        Long userId = userDetails.getUserId();
        List<ArticleDto> results = searchService.getRecommendedArticles(userId, page, limit);
        return ResponseEntity.ok(results);
    }
}
