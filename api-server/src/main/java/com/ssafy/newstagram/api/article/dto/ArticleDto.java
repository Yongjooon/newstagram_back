package com.ssafy.newstagram.api.article.dto;

import com.ssafy.newstagram.domain.news.entity.Article;
import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String description;
    private String url;
    private String thumbnailUrl;
    private String author;
//    private LocalDateTime publishedAt;
//    private LocalDateTime createAt;
//    private LocalDateTime updateAt;
//    private NewsCategory category;

}
