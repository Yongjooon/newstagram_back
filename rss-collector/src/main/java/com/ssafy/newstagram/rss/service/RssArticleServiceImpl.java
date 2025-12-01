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

    /**
     * 공통 로직:
     *  - 주어진 RSS 피드 목록을 돌면서
     *  - RSS HTTP GET → 파싱 → Article 변환 → DB 저장
     */
    private ArticleCollectResultDto collectFromFeeds(List<RssFeed> feeds) {
        ArticleCollectResultDto result = new ArticleCollectResultDto();
        result.setTotalFeeds(feeds.size());

        for (RssFeed feed : feeds) {
            try {
                // 1) RSS 읽기
                SyndFeed syndFeed = readFeed(feed.getRssUrl());
                List<SyndEntry> entries = syndFeed.getEntries();
                result.setTotalItems(result.getTotalItems() + entries.size());

                // 2) 각 entry → Article 로 변환 후 저장
                for (SyndEntry entry : entries) {
                    Article article = convertToArticle(entry, feed);

                    // ON CONFLICT (url) DO NOTHING 을 쓰는 Mapper 메서드
                    int inserted = articleMapper.insertIgnoreOnConflict(article);

                    if (inserted == 1) {
                        result.setInsertedCount(result.getInsertedCount() + 1);
                    } else {
                        // 이미 있는 url → 중복 스킵
                        result.setSkippedCount(result.getSkippedCount() + 1);
                    }
                }

            } catch (Exception e) {
                // 피드 하나에서 에러가 나도 나머지 피드는 계속 수행하도록
                result.addError("Feed ID " + feed.getId()
                        + " (" + feed.getRssUrl() + ") 처리 중 에러: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * RSS URL에서 SyndFeed 읽어오기
     */
    private SyndFeed readFeed(String rssUrl) throws Exception {
        URL url = new URL(rssUrl);
        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(url)) {
            return input.build(reader);
        }
    }

    /**
     * SyndEntry(각 기사) → Article 엔티티로 매핑
     */
    private Article convertToArticle(SyndEntry entry, RssFeed feed) {
        Article article = new Article();

        // 제목
        article.setTitle(entry.getTitle());

        // 설명(description)
        String description = entry.getDescription() != null
                ? entry.getDescription().getValue()
                : null;

        // 내용(content) - contents가 있으면 합쳐서 쓰고, 없으면 description 사용
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

        // 링크(URL)
        article.setUrl(entry.getLink());

        // 썸네일은 나중에 media:thumbnail 같은 태그 파싱해서 넣어도 됨. 일단 null
        article.setThumbnailUrl(null);

        // 작성자
        article.setAuthor(entry.getAuthor());

        // 발행일
        Date publishedDate = entry.getPublishedDate();
        LocalDateTime publishedAt = (publishedDate != null)
                ? LocalDateTime.ofInstant(publishedDate.toInstant(), ZoneId.systemDefault())
                : LocalDateTime.now();
        article.setPublishedAt(publishedAt);

        // 📌 feed 정보 기반으로 외래키 세팅
        article.setFeedId(feed.getId());           // rss_feeds.id
        article.setCategoryId(feed.getCategoryId());
        article.setSourcesId(feed.getSourceId());

        return article;
    }
}
