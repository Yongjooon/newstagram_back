package com.ssafy.newstagram.api.logging.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyCategoryResponseDto {
    private Long id;
    private String description;
    private String name;
}
