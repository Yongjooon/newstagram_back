package com.ssafy.newstagram.api.article.dto;

import java.time.LocalDateTime;

public interface ArticleSearchProjection {
    Long getId();
    String getTitle();
    String getDescription();
    String getUrl();
    String getThumbnailUrl();
    String getAuthor();
    LocalDateTime getPublishedAt();
}
