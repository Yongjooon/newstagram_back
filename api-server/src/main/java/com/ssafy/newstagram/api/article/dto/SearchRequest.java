package com.ssafy.newstagram.api.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SearchRequest {
    @Schema(description = "검색어", example = "최근 경제 뉴스")
    private String query;

    @Schema(description = "검색 결과 개수 제한", example = "10", defaultValue = "10")
    private Integer limit;
}
