package com.ssafy.newstagram.logging.feature.surveysubmit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveySubmitLogDto {
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

    @JsonProperty("category_ids")
    private List<Long> categoryIds;
}
