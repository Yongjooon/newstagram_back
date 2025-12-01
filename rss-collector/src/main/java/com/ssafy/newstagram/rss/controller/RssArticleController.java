package com.ssafy.newstagram.rss.controller;

import com.ssafy.newstagram.rss.dto.ArticleCollectResultDto;
import com.ssafy.newstagram.rss.service.RssArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rss")
@RequiredArgsConstructor
public class RssArticleController {

    private final RssArticleService rssArticleService;

    /**
     * # 전체 기사 불러오기
     * GET /rss/getArticle
     *  - 모든 활성 rss_feeds 기준으로 기사 수집 + DB 저장
     */
    @GetMapping("/getArticle")
    public ResponseEntity<ArticleCollectResultDto> collectAllArticles() {
        ArticleCollectResultDto result = rssArticleService.collectAllArticles();
        return ResponseEntity.ok(result);
    }

    /**
     * # 특정 신문사 기사 불러오기
     * GET /rss/getArticle/{sourceId}
     *  - news_sources.id = sourceId 인 source 의 활성 피드만 대상
     */
    @GetMapping("/getArticle/{sourceId}")
    public ResponseEntity<ArticleCollectResultDto> collectArticlesBySource(
            @PathVariable("sourceId") Long sourceId) {

        ArticleCollectResultDto result = rssArticleService.collectArticlesBySource(sourceId);
        return ResponseEntity.ok(result);
    }

    /**
     * # 특정 신문사 + 특정 카테고리 기사 불러오기
     * GET /rss/getArticle/{sourceId}/{categoryId}
     *  - rss_feeds.source_id = sourceId
     *  - rss_feeds.category_id = categoryId
     */
    @GetMapping("/getArticle/{sourceId}/{categoryId}")
    public ResponseEntity<ArticleCollectResultDto> collectArticlesBySourceAndCategory(
            @PathVariable("sourceId") Long sourceId,
            @PathVariable("categoryId") Long categoryId) {

        ArticleCollectResultDto result =
                rssArticleService.collectAllArticlesBySourceAndCategory(sourceId, categoryId);
        return ResponseEntity.ok(result);
    }
}
