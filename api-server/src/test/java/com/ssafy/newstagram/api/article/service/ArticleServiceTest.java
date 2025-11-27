package com.ssafy.newstagram.api.article.service;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ArticleService articleService;

    private String key = "article:123";
    private Long id = 123L;

    @BeforeEach
    void init() {
        redisTemplate.delete(key);
    }

    @Test
    void hitCache() {
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

}
