package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import com.ssafy.newstagram.domain.news.entity.NewsSource;
import com.ssafy.newstagram.domain.news.entity.RssFeed;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ArticleServiceTest {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private EntityManager em;

    private Long savedArticleId;

    @BeforeEach
    void init() {
        NewsCategory category = em.createQuery("select c from NewsCategory c", NewsCategory.class)
                .setMaxResults(1).getResultList().stream().findFirst().orElseThrow();
        NewsSource source = em.createQuery("select s from NewsSource s", NewsSource.class)
                .setMaxResults(1).getResultList().stream().findFirst().orElseThrow();
        RssFeed feed = em.createQuery("select f from RssFeed f", RssFeed.class)
                .setMaxResults(1).getResultList().stream().findFirst().orElseThrow();

        Article article = Article.builder()
                .title("Test Article")
                .content("Test Content")
                .description("Test Description")
                .url("http://test.com/" + System.currentTimeMillis())
                .publishedAt(LocalDateTime.now())
                .category(category)
                .source(source)
                .feed(feed)
                .build();

        articleRepository.save(article);
        savedArticleId = article.getId();
    }

    @Test
    void cacheHit() {
        String key = "article:123";
        Long id = 123L;
        redisTemplate.delete(key);

        ArticleDto articleDto = ArticleDto.builder()
                .id(id).author("testAuthor").title("testTitle").build();
        redisTemplate.opsForValue().set(key, articleDto);

        ArticleDto result = (ArticleDto) redisTemplate.opsForValue().get(key);
        assertNotNull(result);
        assertEquals(articleDto.getId(), result.getId());
        assertEquals(articleDto.getAuthor(), result.getAuthor());
        assertEquals(articleDto.getTitle(), result.getTitle());

        ArticleDto serviceResult = articleService.getArticleDto(id);
        assertNotNull(serviceResult);
        assertEquals(articleDto.getId(), serviceResult.getId());
        assertEquals(articleDto.getAuthor(), serviceResult.getAuthor());
        assertEquals(articleDto.getTitle(), serviceResult.getTitle());
    }

    @Test
    void cacheMiss() {
        Article jpaResult = articleRepository.findById(savedArticleId).orElseThrow();

        redisTemplate.delete("article:" + savedArticleId);
        ArticleDto result = (ArticleDto) redisTemplate.opsForValue().get("article:" + savedArticleId);
        assertNull(result);

        ArticleDto serviceResult = articleService.getArticleDto(savedArticleId);
        assertNotNull(serviceResult);
        assertEquals(jpaResult.getId(), serviceResult.getId());

        ArticleDto afterMissResult = (ArticleDto) redisTemplate.opsForValue().get("article:" + savedArticleId);
        assertNotNull(afterMissResult);
        assertEquals(jpaResult.getId(), afterMissResult.getId());

    }
}
