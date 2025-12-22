package com.ssafy.newstagram.api.article.repository;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.domain.news.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("""
        SELECT new com.ssafy.newstagram.api.article.dto.ArticleDto(
            a.id,
            a.title,
            a.content,
            a.description,
            a.url,
            a.thumbnailUrl,
            a.author,
            a.publishedAt
        )
        FROM Article a
        WHERE a.id = :id
    """)
    Optional<ArticleDto> findDtoById(@Param("id") Long id);

    List<Article> findByPublishedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Article> findByCategory_IdOrderByPublishedAtDesc(Long categoryId, Pageable pageable);

    @Query(value = "SELECT id, title, content, description, url, thumbnail_url, author, published_at, created_at, updated_at, feed_id, category_id, sources_id, NULL as embedding FROM articles ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<Article> findByEmbeddingSimilarity(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = "SELECT id, title, content, description, url, thumbnail_url, author, published_at, created_at, updated_at, feed_id, category_id, sources_id, NULL as embedding " +
            "FROM articles " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (cast(:startDate as timestamp) IS NULL OR published_at >= cast(:startDate as timestamp)) " +
            "AND (embedding <=> cast(:embedding as vector)) < :threshold " +
            "ORDER BY embedding <=> cast(:embedding as vector) " +
            "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Article> findByEmbeddingSimilarityWithFilters(
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("threshold") double threshold
    );

    @Query(value = "SELECT id, title, content, description, url, thumbnail_url, author, published_at, created_at, updated_at, feed_id, category_id, sources_id, NULL as embedding " +
            "FROM articles " +
            "WHERE (cast(:startDate as timestamp) IS NULL OR published_at >= cast(:startDate as timestamp)) " +
            "ORDER BY embedding <=> cast(:embedding as vector) " +
            "LIMIT :limit", nativeQuery = true)
    List<Article> findByEmbeddingSimilarity(
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("startDate") LocalDateTime startDate
    );

    @Query(value = "SELECT id, title, content, description, url, thumbnail_url, author, published_at, created_at, updated_at, feed_id, category_id, sources_id, NULL as embedding " +
            "FROM articles " +
            "WHERE (:categoryId IS NULL OR category_id = :categoryId) " +
            "AND (cast(:startDate as timestamp) IS NULL OR published_at >= cast(:startDate as timestamp)) " +
            "AND (embedding <=> cast(:embedding as vector)) < :threshold " +
            "ORDER BY embedding <=> cast(:embedding as vector) " +
            "LIMIT :limit", nativeQuery = true)
    List<Article> findCandidatesByEmbedding(
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("threshold") double threshold
    );

}
