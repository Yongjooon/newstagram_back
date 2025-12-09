package com.ssafy.newstagram.api.users.repository;

import com.ssafy.newstagram.domain.user.entity.UserSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistory, Long> {
    List<UserSearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndQuery(Long userId, String query);

    @Modifying
    @Query("UPDATE UserSearchHistory h SET h.query = :newQuery WHERE h.user.id = :userId AND h.query = :oldQuery")
    int updateQueryByUserIdAndQuery(@Param("userId") Long userId, @Param("oldQuery") String oldQuery, @Param("newQuery") String newQuery);
}
