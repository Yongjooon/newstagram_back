package com.ssafy.newstagram.api.article.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EmbeddingResponse {
    private String object;
    private List<EmbeddingData> data;
    private String model;
    private EmbeddingUsage usage;

    @Data
    public static class EmbeddingData{
        private String object;
        private Integer index;
        private List<Double> embedding;
    }

    @Data
    public static class EmbeddingUsage {
        @JsonProperty("prompt_tokens")
        private Integer prompt_tokens;
        @JsonProperty("total_tokens")
        private Integer total_tokens;
    }
}
