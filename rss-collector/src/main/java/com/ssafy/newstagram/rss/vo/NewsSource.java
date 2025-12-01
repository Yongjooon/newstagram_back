package com.ssafy.newstagram.rss.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsSource {
    private Long id;
    private String name;
    private String homepageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



