package com.ssafy.newstagram.api.article.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentAnalysisResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("query")
    private String query;

    @JsonProperty("category")
    private String category;

    @JsonProperty("date_range")
    private int dateRange;

    @JsonProperty("keywords")
    private java.util.List<String> keywords;
}
