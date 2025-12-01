package com.ssafy.newstagram.rss.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;
    private String title;
    private String content;
    private String description;
    private String url;
    private String thumbnailUrl;
    private String author;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long feedId;
    private Long categoryId;
    private Long sourcesId;
}
