package com.ssafy.newstagram.rss.service;

import com.ssafy.newstagram.rss.dto.ArticleCollectResultDto;

public interface RssArticleService {
    ArticleCollectResultDto collectAllArticles();
    ArticleCollectResultDto collectArticlesBySource(Long sourceId);
    ArticleCollectResultDto collectAllArticlesBySourceAndCategory(Long sourceId, Long categoryId);
}
