package com.ssafy.newstagram.api.survey.domain.repository;

import com.ssafy.newstagram.api.survey.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyUserRepository extends JpaRepository<User, Long> {
}
