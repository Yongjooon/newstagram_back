package com.ssafy.newstagram.rss.mapper;

import com.ssafy.newstagram.rss.vo.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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

    // pgvector → 문자열로 조회 (embedding 제대로 가져오기 위한 추가)
    String findEmbeddingLiteralById(@Param("id") Long id);

    // 문자열 literal을 그대로 Article 객체의 embedding 필드에 매핑할 때 사용
    List<Article> findAllWithEmbeddingLiteral();

    List<Article> findArticlesByPeriod(@Param("startPeriod") LocalDateTime startPeriod,
                                       @Param("endPeriod") LocalDateTime endPeriod);
}
