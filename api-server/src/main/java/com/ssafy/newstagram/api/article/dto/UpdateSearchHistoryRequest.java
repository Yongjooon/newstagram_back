package com.ssafy.newstagram.api.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateSearchHistoryRequest {
    @Schema(description = "변경할 히스토리 ID", example = "1")
    private Long historyId;

    @Schema(description = "새로운 검색어", example = "수정된 검색어")
    private String newQuery;
}
