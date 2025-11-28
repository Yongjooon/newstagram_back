package com.ssafy.newstagram.api.article.repository;

import com.ssafy.newstagram.domain.news.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

}
