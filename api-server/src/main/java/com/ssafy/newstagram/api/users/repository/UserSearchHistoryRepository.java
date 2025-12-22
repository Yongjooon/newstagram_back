package com.ssafy.newstagram.api.users.repository;

import com.ssafy.newstagram.domain.user.entity.UserSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistory, Long> {
    List<UserSearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = "SELECT id, query FROM user_search_histories WHERE user_id = :userId ORDER BY created_at DESC", nativeQuery = true)
    List<SearchHistoryProjection> findHistoryNative(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_search_histories WHERE user_id = :userId AND query = :query", nativeQuery = true)
    void deleteByUserIdAndQueryNative(@Param("userId") Long userId, @Param("query") String query);

    void deleteByUserIdAndQuery(Long userId, String query);

    @Modifying
    @Query("UPDATE UserSearchHistory h SET h.query = :newQuery WHERE h.user.id = :userId AND h.query = :oldQuery")
    int updateQueryByUserIdAndQuery(@Param("userId") Long userId, @Param("oldQuery") String oldQuery, @Param("newQuery") String newQuery);

    @Modifying
    @Query(value = "DELETE FROM user_search_histories WHERE id = :id AND user_id = :userId", nativeQuery = true)
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE user_search_histories SET query = :newQuery WHERE id = :id AND user_id = :userId", nativeQuery = true)
    int updateQueryByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId, @Param("newQuery") String newQuery);

    @Modifying
    @Query(value = "UPDATE user_search_histories SET created_at = NOW() WHERE user_id = :userId AND query = :query", nativeQuery = true)
    int updateCreatedAtByUserIdAndQuery(@Param("userId") Long userId, @Param("query") String query);
}
