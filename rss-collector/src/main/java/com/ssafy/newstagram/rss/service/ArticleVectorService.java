package com.ssafy.newstagram.rss.service;

import lombok.AllArgsConstructor;
import lombok.Data;

public interface ArticleVectorService {
    VectorizeResult vectorizeForSource(Long sourceId);

    @Data
    @AllArgsConstructor
    class VectorizeResult {
        private Long sourceId;
        private int totalCount;
        private int successCount;
        private boolean hasGmsError;

        public String getStatus(){
            if(successCount == 0 && hasGmsError){
                return "error";
            }
            return "ok";
        }
    }
}
