package com.ssafy.newstagram.rss.mapper;

import com.ssafy.newstagram.rss.vo.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    int insertIgnoreOnConflict(Article article);

    //특정 신문사에서 임베딩 없는 기사 조회
    List<Article> findArticlesWithoutEmbeddingBySource(@Param("sourceId") Long sourceId);
    //임베딩 벡터 저장
    int updateEmbedding(
            @Param("id") Long id,
            @Param("embedding") String EmbeddingLiteral
    );
}
