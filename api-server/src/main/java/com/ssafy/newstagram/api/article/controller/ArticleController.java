package com.ssafy.newstagram.api.article.controller;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.service.ArticleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleService articleService;
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/detail/{id}")
    public ArticleDto getArticleDto(@PathVariable Long id) {
        return articleService.getArticleDto(id);
    }

}
