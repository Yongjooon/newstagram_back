package com.ssafy.newstagram.logging.domain.repository;

import com.ssafy.newstagram.logging.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query(value = """
        SELECT * FROM (
            SELECT *, 
                   ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY published_at DESC) as rn
            FROM articles
            WHERE category_id IN :categoryIds
        ) t
        WHERE t.rn <= 2
        """, nativeQuery = true)
    List<Article> findTop2ArticlesByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
}
