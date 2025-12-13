package com.ssafy.newstagram.rss.clustering.repository;

import com.ssafy.newstagram.domain.recommend.entity.PeriodRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodRecommendationRepository extends JpaRepository<PeriodRecommendation, Long> {

}
