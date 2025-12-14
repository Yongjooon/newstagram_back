package com.ssafy.newstagram.api.logging.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionLogsDto {

    /**
     * 현재 해당 DTO는 UserInteractionLogs로 데이터를 전송만 하는 목적으로 사용
     * id는 DB에 저장되는 순간에 자동으로 값을 넣어주는 PK로 현재 사용하지 않음
     */
    //private Long id;

    @Builder.Default
    @JsonProperty("interaction_type")
    private String interactionType= "CLICK";

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_agent")
    private String userAgent;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("article_id")
    private Long articleId;
}
