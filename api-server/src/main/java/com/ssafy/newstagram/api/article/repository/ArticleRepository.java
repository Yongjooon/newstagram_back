package com.ssafy.newstagram.api.article.repository;

import com.ssafy.newstagram.domain.news.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT id, title, content, description, url, thumbnail_url, author, published_at, created_at, updated_at, feed_id, category_id, sources_id, NULL as embedding FROM articles ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<Article> findByEmbeddingSimilarity(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = "SELECT id, title, content, description, url, thumbnail_url, author, published_at, created_at, updated_at, feed_id, category_id, sources_id, NULL as embedding " +
            "FROM articles " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (cast(:startDate as timestamp) IS NULL OR published_at >= cast(:startDate as timestamp)) " +
            "ORDER BY embedding <=> cast(:embedding as vector) " +
            "LIMIT :limit", nativeQuery = true)
    List<Article> findByEmbeddingSimilarityWithFilters(
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate
    );

}
