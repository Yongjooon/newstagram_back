package com.ssafy.newstagram.logging.feature.articleclick.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.newstagram.logging.domain.UserInteractionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleClickLogDto {

    /**
     * 현재 해당 DTO는 UserInteractionLogs로 데이터를 전송만 하는 목적으로 사용
     * id는 DB에 저장되는 순간에 자동으로 값을 넣어주는 PK로 현재 사용하지 않음
     */
    //private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("article_id")
    private Long articleId;

    @Builder.Default
    @JsonProperty("interaction_type")
    private String interactionType= "CLICK";

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public UserInteractionLog toEntity() {
        return UserInteractionLog.builder()
                .userId(this.userId)
                .articleId(this.articleId)
                .interactionType(this.interactionType)
                .sessionId(this.sessionId)
                .userAgent(this.userAgent)
                .ipAddress(this.ipAddress)
                .createdAt(this.createdAt != null ? this.createdAt : LocalDateTime.now())
                .build();
    }
}
