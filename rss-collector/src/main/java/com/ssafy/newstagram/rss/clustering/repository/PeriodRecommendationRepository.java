package com.ssafy.newstagram.rss.clustering.repository;

import com.ssafy.newstagram.domain.recommend.entity.PeriodRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PeriodRecommendationRepository extends JpaRepository<PeriodRecommendation, Long> {

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO period_recommendations (period_type, article_id, ranking, score) VALUES (:periodType, :articleId, :ranking, :score)",
            nativeQuery = true
    )
    void insertRankingAndScore(
            @Param("periodType") String periodType,
            @Param("ranking") int ranking,
            @Param("score") double score,
            @Param("articleId") Long articleId
    );

}
