package com.ssafy.newstagram.logging.domain.repository;

import com.ssafy.newstagram.logging.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
