package com.ssafy.newstagram.logging.domain.repository;

import com.ssafy.newstagram.logging.domain.UserInteractionLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInteractionLogRepository extends JpaRepository<UserInteractionLog, Long> {
    // 최근 10개 로그만 가져오기 (시간 내림차순)
    List<UserInteractionLog> findByUserId(Long userId, Pageable pageable);
}
