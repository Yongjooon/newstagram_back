package com.ssafy.newstagram.api.article.controller;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.dto.SearchRequest;
import com.ssafy.newstagram.api.article.dto.UpdateSearchHistoryRequest;
import com.ssafy.newstagram.api.article.service.SearchService;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Search", description = "Semantic Search API")
@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Semantic Search", description = "Search articles using Rule-based + LLM Hybrid approach.")
    @PostMapping
    public ResponseEntity<List<ArticleDto>> searchArticles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SearchRequest request) {
        
        String query = request.getQuery();
        Integer limit = request.getLimit();
        
        if (limit == null) {
            limit = 10; // Default limit
        }

        Long userId = userDetails.getUserId();

        List<ArticleDto> results = searchService.searchArticles(userId, query, limit);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Semantic Search (Test)", description = "Search articles using Rule-based + LLM Hybrid approach without user authentication.")
    @PostMapping("/test")
    public ResponseEntity<List<ArticleDto>> searchArticlesTest(@RequestBody SearchRequest request) {
        
        String query = request.getQuery();
        Integer limit = request.getLimit();
        
        if (limit == null) {
            limit = 10; // Default limit
        }

        // Skip saving history and directly search
        List<ArticleDto> results = searchService.getCachedSearchResults(query, limit);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get Search History", description = "Get recent search queries for the user.")
    @GetMapping("/history")
    public ResponseEntity<List<String>> getSearchHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        List<String> history = searchService.getSearchHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Delete Search History", description = "Delete a specific search query from history.")
    @DeleteMapping("/history")
    public ResponseEntity<Void> deleteSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String query) {
        Long userId = userDetails.getUserId();
        searchService.deleteSearchHistory(userId, query);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update Search History", description = "Rename a specific search query in history.")
    @PutMapping("/history")
    public ResponseEntity<Void> updateSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateSearchHistoryRequest request) {
        Long userId = userDetails.getUserId();
        String oldQuery = request.getOldQuery();
        String newQuery = request.getNewQuery();
        
        if (oldQuery == null || newQuery == null) {
            return ResponseEntity.badRequest().build();
        }

        searchService.updateSearchHistory(userId, oldQuery, newQuery);
        return ResponseEntity.ok().build();
    }
}
