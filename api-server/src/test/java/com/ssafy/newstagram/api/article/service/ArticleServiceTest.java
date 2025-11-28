package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import com.ssafy.newstagram.api.article.repository.ArticleRepository;
import com.ssafy.newstagram.domain.news.entity.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ArticleService articleService;


    @BeforeEach
    void init() {
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
        Article jpaResult = articleRepository.findById(1L).orElseThrow();

        redisTemplate.delete("article:1");
        ArticleDto result = (ArticleDto) redisTemplate.opsForValue().get("article:1");
        assertNull(result);

        ArticleDto serviceResult = articleService.getArticleDto(1L);
        assertNotNull(serviceResult);
        assertEquals(jpaResult.getId(), serviceResult.getId());

        ArticleDto afterMissResult = (ArticleDto) redisTemplate.opsForValue().get("article:1");
        assertNotNull(afterMissResult);
        assertEquals(jpaResult.getId(), afterMissResult.getId());

    }




}
