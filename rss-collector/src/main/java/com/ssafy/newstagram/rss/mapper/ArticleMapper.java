package com.ssafy.newstagram.rss.mapper;

import com.ssafy.newstagram.rss.vo.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper {
    int insertIgnoreOnConflict(Article article);
}
