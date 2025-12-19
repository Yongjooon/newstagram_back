package com.ssafy.newstagram.api.survey.domain.repository;

import com.ssafy.newstagram.domain.news.entity.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyNewsCategoryRepository extends JpaRepository<NewsCategory, Long> {
    List<NewsCategory> findAllByIdNotIn(List<Long> ids);
}
