package com.ssafy.newstagram.api.survey.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SurveySubmitRequestDto {
    private List<Long> categoryIds;
}
