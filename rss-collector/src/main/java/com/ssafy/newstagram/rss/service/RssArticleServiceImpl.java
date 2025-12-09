package com.ssafy.newstagram.rss.service;

import com.ssafy.newstagram.rss.dto.ArticleCollectResultDto;
import com.ssafy.newstagram.rss.mapper.ArticleMapper;
import com.ssafy.newstagram.rss.mapper.RssFeedMapper;
import com.ssafy.newstagram.rss.util.ArticleTextNormalizer;
import com.ssafy.newstagram.rss.util.NormalizedText;
import com.ssafy.newstagram.rss.vo.Article;
import com.ssafy.newstagram.rss.vo.RssFeed;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.Metadata;
import com.rometools.modules.mediarss.types.Thumbnail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (feed == null) {
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
                SyndFeed syndFeed = readFeed(feed.getRssUrl());
                List<SyndEntry> entries = syndFeed.getEntries();
                result.setTotalItems(result.getTotalItems() + entries.size());

                for (SyndEntry entry : entries) {
                    Article article = convertToArticle(entry, feed);

                    int inserted = articleMapper.insertIgnoreOnConflict(article);

                    if (inserted == 1) {
                        result.setInsertedCount(result.getInsertedCount() + 1);
                    } else {
                        result.setSkippedCount(result.getSkippedCount() + 1);
                    }
                }

            } catch (Exception e) {
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


    private String extractThumbnailUrl(SyndEntry entry, String rawContent, String rawDescription) {
        try {
            if (entry.getEnclosures() != null) {
                for (var enc : entry.getEnclosures()) {
                    String url = enc.getUrl();
                    String type = enc.getType();
                    if (url != null && !url.isBlank()) {
                        if (type == null || type.toLowerCase().startsWith("image")) {
                            return url;
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }

        try {
            MediaEntryModule media = (MediaEntryModule) entry.getModule(MediaEntryModule.URI);
            if (media != null) {
                Metadata meta = media.getMetadata();
                if (meta != null) {
                    Thumbnail[] thumbs = meta.getThumbnail();
                    if (thumbs != null && thumbs.length > 0) {
                        Thumbnail t = thumbs[0];
                        if (t != null && t.getUrl() != null) {
                            String url = t.getUrl().toString();
                            if (url != null && !url.isBlank()) {
                                return url;
                            }
                        }
                    }
                }

                MediaContent[] mediaContents = media.getMediaContents();
                if (mediaContents != null) {
                    for (MediaContent mc : mediaContents) {
                        if (mc == null) continue;

                        String type = mc.getType();
                        String medium = mc.getMedium();
                        String refUrl = (mc.getReference() != null)
                                ? mc.getReference().toString()
                                : null;

                        boolean isImage =
                                (type != null && type.toLowerCase().startsWith("image"))
                                        || (medium != null && medium.equalsIgnoreCase("image"));

                        if (isImage && refUrl != null && !refUrl.isBlank()) {
                            return refUrl;
                        }

                        Metadata cMeta = mc.getMetadata();
                        if (cMeta != null) {
                            Thumbnail[] cThumbs = cMeta.getThumbnail();
                            if (cThumbs != null && cThumbs.length > 0) {
                                Thumbnail t2 = cThumbs[0];
                                if (t2 != null && t2.getUrl() != null) {
                                    String url = t2.getUrl().toString();
                                    if (url != null && !url.isBlank()) {
                                        return url;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }

        String fromContent = extractFirstImgSrc(rawContent);
        if (fromContent != null) {
            return fromContent;
        }

        String fromDescription = extractFirstImgSrc(rawDescription);
        if (fromDescription != null) {
            return fromDescription;
        }

        return null;
    }

    private String extractFirstImgSrc(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        Pattern p = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        while (m.find()) {
            String url = m.group(1);
            if (!isTrackingOrPixelUrl(url)) {
                return url;
            }
        }
        return null;
    }

    private boolean isTrackingOrPixelUrl(String url) {
        if (url == null) return true;
        String lower = url.toLowerCase();

        if (lower.contains("tracking_rss")) return true;
        if (lower.contains("tracking") && lower.contains("rss")) return true;
        if (lower.contains("pixel")) return true;
        if (lower.contains("beacon")) return true;
        if (lower.contains("logger")) return true;

        return false;
    }

    private Article convertToArticle(SyndEntry entry, RssFeed feed) {
        Article article = new Article();

        // 제목
        article.setTitle(entry.getTitle());

        // 설명
        String description = (entry.getDescription() != null)
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

        // 정규화
        Long sourceId = feed.getSourceId();
        NormalizedText normalized = ArticleTextNormalizer.normalize(sourceId, content, description);
        article.setDescription(normalized.getDescription());
        article.setContent(normalized.getContent());

        // 링크
        article.setUrl(entry.getLink());

        // 썸네일
        String rawDescription = (entry.getDescription() != null)
                ? entry.getDescription().getValue()
                : null;

        StringBuilder rcontentBuilder = new StringBuilder();
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            for (SyndContent c : entry.getContents()) {
                if (c != null && c.getValue() != null) {
                    rcontentBuilder.append(c.getValue()).append("\n");
                }
            }
        }
        String rawContent = rcontentBuilder.length() > 0 ? rcontentBuilder.toString() : rawDescription;

        // 썸네일 추출
        String thumbnailUrl = extractThumbnailUrl(entry, rawContent, rawDescription);
        article.setThumbnailUrl(thumbnailUrl);

        // 작성자
        article.setAuthor(entry.getAuthor());

        // 발행일
        Date publishedDate = entry.getPublishedDate();
        LocalDateTime publishedAt = (publishedDate != null)
                ? LocalDateTime.ofInstant(publishedDate.toInstant(), ZoneId.systemDefault())
                : LocalDateTime.now();
        article.setPublishedAt(publishedAt);

        // feed 기반 FK
        article.setFeedId(feed.getId());
        article.setCategoryId(feed.getCategoryId());
        article.setSourcesId(feed.getSourceId());

        return article;
    }
}
