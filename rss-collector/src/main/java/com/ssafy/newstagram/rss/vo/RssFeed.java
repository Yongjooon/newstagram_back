package com.ssafy.newstagram.rss.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RssFeed {
    private Long id;
    private String name;
    private String rssUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastFetchedAt;
    private Long categoryId;
    private Long sourceId;
}
