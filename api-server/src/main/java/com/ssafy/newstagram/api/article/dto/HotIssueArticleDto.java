package com.ssafy.newstagram.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotIssueArticleDto {

    private Long articleId;
    private Integer rankInGroup;   // 클러스터 내 순위
}
