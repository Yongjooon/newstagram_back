package com.ssafy.newstagram.api.article.repository;

import com.ssafy.newstagram.api.article.dto.HotIssueItemDto;
import com.ssafy.newstagram.domain.recommend.entity.PeriodRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PeriodRecommendationRepository extends JpaRepository<PeriodRecommendation, Long> {

    @Query(value = """
        SELECT
            ranked.id AS id,
            ranked.article_id AS articleId,
            ranked.group_ranking AS groupRanking,
            ranked.rank_in_group AS rankInGroup
        FROM (
            SELECT
                pr.id,
                pr.article_id,
                pr.ranking AS group_ranking,
                ROW_NUMBER() OVER (
                    PARTITION BY pr.ranking
                    ORDER BY pr.score ASC
                ) AS rank_in_group
            FROM period_recommendations pr
            WHERE pr.period_type = :periodType
              AND pr.period_start = :periodStart
        ) ranked
        WHERE ranked.rank_in_group <= :limitCount
        ORDER BY ranked.group_ranking ASC, ranked.rank_in_group ASC
        """,
            nativeQuery = true)
    List<HotIssueItemDto> findTopNPerRanking(
            @Param("periodType") String periodType,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("limitCount") int limitCount
    );

    @Query(value = """
        SELECT
            ranked.id AS id,
            ranked.article_id AS articleId,
            ranked.group_ranking AS groupRanking,
            ranked.rank_in_group AS rankInGroup
        FROM (
            SELECT
                pr.id,
                pr.article_id,
                pr.ranking AS group_ranking,
                ROW_NUMBER() OVER (
                    PARTITION BY pr.ranking
                    ORDER BY pr.score ASC
                ) AS rank_in_group
            FROM period_recommendations pr
            WHERE pr.period_type = :periodType
              AND pr.period_start = :periodStart
              AND (:cursor IS NULL OR pr.ranking > :cursor AND pr.ranking <= :cursor+:groupSize)
        ) ranked
        WHERE ranked.rank_in_group <= :limitCount
        ORDER BY ranked.group_ranking ASC, ranked.rank_in_group ASC
        """,
            nativeQuery = true)
    List<HotIssueItemDto> findTopNPerRankingWithPaging(
            @Param("periodType") String periodType,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("limitCount") int limitCount,   // 그룹당 기사 수 (ex: 5)
            @Param("groupSize") int groupSize,     // 한 페이지 그룹 수 (ex: 10)
            @Param("cursor") Integer cursor        // 마지막 group_ranking
    );

    // 특정 기간 단위 조회
    List<PeriodRecommendation> findByPeriodType(String periodType);

    // 기간 범위 조회
    List<PeriodRecommendation> findByPeriodTypeAndPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(
            String periodType, LocalDateTime start, LocalDateTime end);

    // 기간+랭킹 기준 조회
    List<PeriodRecommendation> findByPeriodTypeAndPeriodStartAndPeriodEndOrderByRankingAsc(
            String periodType, LocalDateTime start, LocalDateTime end);

    // 특정 기간 삭제
    void deleteByPeriodTypeAndPeriodStartAndPeriodEnd(String periodType, LocalDateTime start, LocalDateTime end);
}
