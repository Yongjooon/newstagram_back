package com.ssafy.newstagram.api.article.repository;

import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {
    Optional<NewsCategory> findByName(String name);
}
