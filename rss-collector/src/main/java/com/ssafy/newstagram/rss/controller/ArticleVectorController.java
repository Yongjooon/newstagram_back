package com.ssafy.newstagram.rss.controller;

import com.ssafy.newstagram.rss.service.ArticleVectorService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vector")
@RequiredArgsConstructor
public class ArticleVectorController {
    private final ArticleVectorService articleVectorService;

    @PostMapping("/embedding/{sourceId}")
    public ResponseEntity<EmbeddingResult> embedBySource(@PathVariable Long sourceId) {
        ArticleVectorService.VectorizeResult result = articleVectorService.vectorizeForSource(sourceId);

        String status = result.getStatus();
        EmbeddingResult body = new EmbeddingResult(result.getSourceId(), status);

        if("error".equals(status)){
            return ResponseEntity.status(502).body(body);
        }
        return ResponseEntity.ok(body);

    }

    @Data
    @AllArgsConstructor
    static class EmbeddingResult {
        private Long sourceId;
        private String status;
    }


}
