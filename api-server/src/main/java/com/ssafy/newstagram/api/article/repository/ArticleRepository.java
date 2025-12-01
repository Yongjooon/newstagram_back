package com.ssafy.newstagram.api.article.repository;

import com.ssafy.newstagram.domain.news.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);

}
