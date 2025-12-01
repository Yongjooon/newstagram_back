package com.ssafy.newstagram.rss.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ArticleCollectResultDto {
    private int totalFeeds;
    private int totalItems;
    private int insertedCount;
    private int skippedCount;
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }
}
