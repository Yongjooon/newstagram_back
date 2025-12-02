package com.ssafy.newstagram.rss.service;

import com.ssafy.newstagram.rss.dto.ArticleCollectResultDto;
import com.ssafy.newstagram.rss.mapper.ArticleMapper;
import com.ssafy.newstagram.rss.mapper.RssFeedMapper;
import com.ssafy.newstagram.rss.vo.Article;
import com.ssafy.newstagram.rss.vo.RssFeed;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RssArticleServiceImpl implements RssArticleService {

    private final RssFeedMapper rssFeedMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public ArticleCollectResultDto collectAllArticles() {
        List<RssFeed> feeds = rssFeedMapper.findAllActiveFeeds();
        return collectFromFeeds(feeds);
    }

    @Override
    @Transactional
    public ArticleCollectResultDto collectArticlesBySource(Long sourceId) {
        List<RssFeed> feeds = rssFeedMapper.findActiveFeedsBySourceId(sourceId);
        return collectFromFeeds(feeds);
    }

    @Override
    @Transactional
    public ArticleCollectResultDto collectAllArticlesBySourceAndCategory(Long sourceId, Long categoryId) {
        List<RssFeed> feeds = rssFeedMapper.findActiveFeedsBySourceIdAndCategoryId(sourceId, categoryId);
        return collectFromFeeds(feeds);
    }

    @Override
    @Transactional
    public ArticleCollectResultDto collectArticlesByFeed(Long feedId) {
        RssFeed feed = rssFeedMapper.findById(feedId);
        ArticleCollectResultDto result = new ArticleCollectResultDto();

        if(feed == null){
            result.addError("Feed가 없습니다., feedId = " + feedId);
            result.setTotalFeeds(0);
            return result;
        }

        return collectFromFeeds(List.of(feed));
    }


    private ArticleCollectResultDto collectFromFeeds(List<RssFeed> feeds) {
        ArticleCollectResultDto result = new ArticleCollectResultDto();
        result.setTotalFeeds(feeds.size());

        for (RssFeed feed : feeds) {
            try {
                //RSS 읽기
                SyndFeed syndFeed = readFeed(feed.getRssUrl());
                List<SyndEntry> entries = syndFeed.getEntries();
                result.setTotalItems(result.getTotalItems() + entries.size());

                for (SyndEntry entry : entries) {
                    Article article = convertToArticle(entry, feed);

                    int inserted = articleMapper.insertIgnoreOnConflict(article);

                    if (inserted == 1) {
                        result.setInsertedCount(result.getInsertedCount() + 1);
                    } else {
                        // 중복 스킵
                        result.setSkippedCount(result.getSkippedCount() + 1);
                    }
                }

            } catch (Exception e) {
                // 피드 하나에서 에러가 나도 나머지 피드는 계속 수행
                result.addError("Feed ID " + feed.getId()
                        + " (" + feed.getRssUrl() + ") 처리 중 에러: " + e.getMessage());
            }
        }

        return result;
    }


    private SyndFeed readFeed(String rssUrl) throws Exception {
        URL url = new URL(rssUrl);
        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(url.openStream())) {
            return input.build(reader);
        }
    }


    private Article convertToArticle(SyndEntry entry, RssFeed feed) {
        Article article = new Article();

        // 제목
        article.setTitle(entry.getTitle());

        // 설명
        String description = entry.getDescription() != null
                ? entry.getDescription().getValue()
                : null;

        // 내용
        StringBuilder contentBuilder = new StringBuilder();
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            for (SyndContent c : entry.getContents()) {
                if (c != null && c.getValue() != null) {
                    contentBuilder.append(c.getValue()).append("\n");
                }
            }
        }
        String content = contentBuilder.length() > 0 ? contentBuilder.toString() : description;

        article.setDescription(description);
        article.setContent(content);

        // 링크
        article.setUrl(entry.getLink());

        // 썸네일
        article.setThumbnailUrl(null);

        // 작성자
        article.setAuthor(entry.getAuthor());

        // 발행일
        Date publishedDate = entry.getPublishedDate();
        LocalDateTime publishedAt = (publishedDate != null)
                ? LocalDateTime.ofInstant(publishedDate.toInstant(), ZoneId.systemDefault())
                : LocalDateTime.now();
        article.setPublishedAt(publishedAt);

        // feed 정보 기반으로 외래키 세팅
        article.setFeedId(feed.getId());
        article.setCategoryId(feed.getCategoryId());
        article.setSourcesId(feed.getSourceId());

        return article;
    }
}
