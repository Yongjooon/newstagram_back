package com.ssafy.newstagram.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotIssueGroupDto {

    private int groupRanking;      // 클러스터 랭킹 (1, 2, 3 ...)

    private List<HotIssueArticleDto> articles;
}
