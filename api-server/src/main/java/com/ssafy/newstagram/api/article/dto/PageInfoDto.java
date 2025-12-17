package com.ssafy.newstagram.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoDto {

    private boolean hasNext;
    private Integer nextCursor;

}
