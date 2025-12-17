package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.*;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.api.article.repository.PeriodRecommendationRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import com.ssafy.newstagram.domain.util.period.Period;
import com.ssafy.newstagram.domain.util.period.PeriodCalculator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotIssueService {

    private final PeriodRecommendationRepository periodRecommendationRepository;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_ISSUE_KEY_PREFIX = "hot-issue:";
    private static final String ARTICLE_KEY_PREFIX = "article:";

    /* =======================
       Public API
     ======================= */

    public HotIssueResponse getHotIssueResponse(
            String periodType,
            int groupSize,      // 한 페이지에 보여줄 그룹 수
            int limitCount,     // 그룹당 기사 수 (ex: 5)
            Integer cursor      // 마지막 groupRanking
    ) {
        HotIssueSetDto hotIssueSet =
                getOrLoadHotIssueSet(periodType, groupSize, limitCount, cursor);

        // pageInfo 계산
        PageInfoDto pageInfo = buildPageInfo(hotIssueSet.getGroups(), groupSize);

        return toHotIssueResponse(
                hotIssueSet,
                pageInfo.isHasNext(),
                pageInfo.getNextCursor()
        );
    }

    /* =======================
       HotIssueSet 조회 (Redis + DB)
     ======================= */

    @Transactional
    protected HotIssueSetDto getOrLoadHotIssueSet(
            String periodType,
            int groupSize,
            int limitCount,
            Integer cursor
    ) {
        Period period = Period.valueOf(periodType);
        PeriodCalculator calculator = period.getCalculator();

        LocalDateTime periodStart = calculator.getStart();
        return loadByPeriod(
                periodType,
                period,
                periodStart,
                groupSize,
                limitCount,
                cursor,
                calculator
        );
    }

    private HotIssueSetDto loadByPeriod(
            String periodType,
            Period period,
            LocalDateTime periodStart,
            int groupSize,
            int limitCount,
            Integer cursor,
            PeriodCalculator calculator
    ) {
        String periodKey = buildPeriodKey(period.name(), periodStart, cursor);

        // Redis 조회
        HotIssueSetDto cached = getFromRedis(periodKey, HotIssueSetDto.class);
        if (cached != null) {
            log.info("[REDIS] HIT key={}", periodKey);
            return cached;
        }

        log.info("[REDIS] MISS key={}", periodKey);

        List<HotIssueItemDto> results =
                periodRecommendationRepository.findTopNPerRankingWithPaging(
                        periodType,
                        periodStart,
                        limitCount,
                        groupSize,
                        cursor
                );

        // 결과 없으면 이전 period
        if (results.isEmpty()) {
            log.info("현재 period 결과 없음 → 이전 period 조회");
            LocalDateTime beforeStart = calculator.getBeforeStart();
            return loadByPeriod(
                    periodType,
                    period,
                    beforeStart,
                    groupSize,
                    limitCount,
                    cursor,
                    calculator
            );
        }

        HotIssueSetDto response =
                toHotIssueSetDto(periodKey, periodType, results);

        redisTemplate.opsForValue().set(periodKey, response);
        return response;
    }

    /* =======================
       PageInfo 계산
     ======================= */

    private PageInfoDto buildPageInfo(
            List<HotIssueGroupDto> groups,
            int groupSize
    ) {
        if (groups.isEmpty()) {
            return PageInfoDto.builder()
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        boolean hasNext = groups.size() == groupSize;

        Integer nextCursor = hasNext
                ? groups.get(groups.size() - 1).getGroupRanking()
                : null;

        return PageInfoDto.builder()
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /* =======================
       DTO 변환
     ======================= */

    private HotIssueSetDto toHotIssueSetDto(
            String periodKey,
            String periodType,
            List<HotIssueItemDto> results
    ) {
        Map<Integer, List<HotIssueItemDto>> grouped =
                results.stream()
                        .collect(Collectors.groupingBy(
                                HotIssueItemDto::getGroupRanking,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<HotIssueGroupDto> groups =
                grouped.entrySet().stream()
                        .map(this::toHotIssueGroupDto)
                        .toList();

        return HotIssueSetDto.builder()
                .periodKey(periodKey)
                .periodType(periodType)
                .groups(groups)
                .build();
    }

    private HotIssueGroupDto toHotIssueGroupDto(
            Map.Entry<Integer, List<HotIssueItemDto>> entry
    ) {
        List<HotIssueArticleDto> articles =
                entry.getValue().stream()
                        .sorted(Comparator.comparingInt(HotIssueItemDto::getRankInGroup))
                        .map(item -> HotIssueArticleDto.builder()
                                .articleId(item.getArticleId())
                                .rankInGroup(item.getRankInGroup())
                                .build())
                        .toList();

        return HotIssueGroupDto.builder()
                .groupRanking(entry.getKey())
                .articles(articles)
                .build();
    }

    /* =======================
       Response 변환
     ======================= */

    private HotIssueResponse toHotIssueResponse(
            HotIssueSetDto hotIssueSet,
            boolean hasNext,
            Integer nextCursor
    ) {
        List<GroupRandomArticleResponse> groups =
                hotIssueSet.getGroups().stream()
                        .map(this::toGroupRandomArticleResponse)
                        .toList();

        return HotIssueResponse.builder()
                .groups(groups)
                .pageInfo(PageInfoDto.builder()
                        .hasNext(hasNext)
                        .nextCursor(nextCursor)
                        .build())
                .build();
    }

    private GroupRandomArticleResponse toGroupRandomArticleResponse(
            HotIssueGroupDto group
    ) {
        HotIssueArticleDto picked = pickRandomArticle(group.getArticles());
        log.info("picked rankInGroup: {}", picked.getRankInGroup());
        log.info("picked articleId: {}", picked.getArticleId());
        ArticleDto article = getArticleWithLookAside(picked.getArticleId());
        return GroupRandomArticleResponse.builder()
                .groupId(group.getGroupRanking())
                .rankInGroup(picked.getRankInGroup())
                .article(article)
                .build();
    }

    /* =======================
       Article (Redis Look-aside)
     ======================= */

    private ArticleDto getArticleWithLookAside(Long articleId) {
        String key = ARTICLE_KEY_PREFIX + articleId;

        ArticleDto cached = getFromRedis(key, ArticleDto.class);
        if (cached != null) {
            return cached;
        }

        ArticleDto dto = articleRepository.findDtoById(articleId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Article not found id=" + articleId)
                );

        redisTemplate.opsForValue().set(key, dto);

        return dto;
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

    /* =======================
       Utils
     ======================= */

    private HotIssueArticleDto pickRandomArticle(List<HotIssueArticleDto> articles) {
        int index = ThreadLocalRandom.current().nextInt(articles.size());
        return articles.get(index);
    }

    private String buildPeriodKey(
            String periodType,
            LocalDateTime start,
            Integer cursor
    ) {
        return HOT_ISSUE_KEY_PREFIX +
                periodType + ":" +
                start.format(DateTimeFormatter.ofPattern("yyyyMMddHH")) +
                ":" + (cursor == null ? "first" : cursor);
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromRedis(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return type.isInstance(value) ? (T) value : null;
    }
}

