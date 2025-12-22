package com.ssafy.newstagram.api.article.controller;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.dto.HotIssueResponse;
import com.ssafy.newstagram.api.article.service.ArticleService;
import com.ssafy.newstagram.api.article.service.HomeIssueService;
import com.ssafy.newstagram.api.article.service.HotIssueService;
import com.ssafy.newstagram.api.article.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final HotIssueService hotIssueService;
    private final HomeIssueService homeIssueService;
    private static final int GROUP_SIZE = 10;
    private static final int LIMIT_COUNT = 5;


    @GetMapping("/detail/{id}")
    public ArticleDto getArticleDto(@PathVariable Long id) {
        return articleService.getArticleDto(id);
    }

//    @GetMapping("/hot-issues")
//    public ResponseEntity<HotIssueSetDto> getHotIssues(
//            @RequestParam(name = "periodType") String periodType,
//            @RequestParam(name = "limitCount", defaultValue = LIMIT_COUNT) int limitCount
//    ) {
//        return ResponseEntity.ok(
//                hotIssueService.getPeriodRecommendationsTopNPerRankingByPeriodType(
//                        periodType,
//                        limitCount
//                )
//        );
//    }

    @GetMapping("/hot-issues/{periodType}/{cursor}")
    public ResponseEntity<HotIssueResponse> getHotIssueArticleRandomly(
            @PathVariable(name = "periodType") String periodType,
            @PathVariable(name = "cursor") Integer cursor
    ) {
        return ResponseEntity.ok(
                hotIssueService.getHotIssueResponse(
                        periodType, GROUP_SIZE, LIMIT_COUNT, cursor
                )
        );
    }

    @GetMapping("/home-issues")
    public List<ArticleDto> getHomeIssue() {
        String vector = Constants.HOME_VECTOR;
        return homeIssueService.findByEmbeddingSimilarity(vector, 50, LocalDateTime.now().minusDays(3));
    }
}
