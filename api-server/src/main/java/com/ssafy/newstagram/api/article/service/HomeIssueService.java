package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeIssueService {

    private final ArticleRepository articleRepository;

    public List<ArticleDto> findByEmbeddingSimilarity(String embedding, int limit, LocalDateTime startDate) {
        List<Article> articles = articleRepository.findByEmbeddingSimilarity(embedding, limit, startDate);
        // Article 리스트를 ArticleDto 리스트로 변환
        return articles.stream()
                .map(this::toArticleDto)  // toArticleDto 메소드 사용
                .collect(Collectors.toList());
    }


    private ArticleDto toArticleDto(Article article) {
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

}
