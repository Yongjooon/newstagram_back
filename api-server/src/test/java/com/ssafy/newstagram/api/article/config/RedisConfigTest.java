package com.ssafy.newstagram.api.article.config;

import com.ssafy.newstagram.api.article.dto.ArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RedisConfigTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void init() {
        stringRedisTemplate.delete("testStringKey");
        redisTemplate.delete("testArticleDtoKey");
        redisTemplate.delete("testListKey");
    }

    @Test
    void testStringWithStringRedisTemplate() {
        stringRedisTemplate.opsForValue().set("testStringKey", "testStringValue");
        String result = stringRedisTemplate.opsForValue().get("testStringKey");
        assertEquals("testStringValue",result);
    }

    @Test
    void testArticleDtoWithRedisTemplate() {
        ArticleDto articleDto = ArticleDto.builder()
                .id(123L).author("testAuthor").title("testTitle").build();
        redisTemplate.opsForValue().set("testArticleDtoKey", articleDto);
        ArticleDto result = (ArticleDto) redisTemplate.opsForValue().get("testArticleDtoKey");
        assertNotNull(result);
        assertEquals(articleDto.getId(), result.getId());
        assertEquals(articleDto.getAuthor(), result.getAuthor());
        assertEquals(articleDto.getTitle(), result.getTitle());
    }

    @Test
    void testListWithRedisTemplate() {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        ArticleDto articleDto1 = ArticleDto.builder()
                .id(1L).author("testAuthor1").title("testTitle1").build();
        ArticleDto articleDto2 = ArticleDto.builder()
                .id(2L).author("testAuthor2").title("testTitle2").build();

        listOps.rightPush("testListKey", articleDto1);
        listOps.rightPush("testListKey", articleDto2);

        assertEquals(2, listOps.size("testListKey"));
    }

}
