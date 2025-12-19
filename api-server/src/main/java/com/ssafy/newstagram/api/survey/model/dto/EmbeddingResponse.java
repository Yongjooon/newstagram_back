package com.ssafy.newstagram.api.survey.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 불필요한 필드는 무시
public class EmbeddingResponse {

    private String object;
    private List<EmbeddingData> data;
    private Usage usage;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class EmbeddingData {
        private String object;
        private int index;
        private List<Double> embedding; // 벡터 데이터
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Usage {
        private int prompt_tokens;
        private int total_tokens;
    }
}