package com.ssafy.newstagram.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ClusteringSummaryResponse {

    private int totalClusterCount;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private List<ClusterSummaryItem> clusters;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ClusterSummaryItem {
        private int clusterId;
        private int articleCount;
        private String mainArticleTitle;
    }
}
