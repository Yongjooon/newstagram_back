package com.ssafy.newstagram.rss.mapper;

import com.ssafy.newstagram.rss.vo.RssFeed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

@Mapper
public interface RssFeedMapper {
    List<RssFeed> findAllActiveFeeds();
    List<RssFeed> findActiveFeedsBySourceId(@Param("sourceId") Long sourceId);
    List<RssFeed> findActiveFeedsBySourceIdAndCategoryId(@Param("sourceId") Long sourceId, @Param("categoryId") Long categoryId);
}
