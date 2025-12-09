package com.ssafy.newstagram.api.users.repository;

import com.ssafy.newstagram.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE email = :email", nativeQuery = true)
    boolean existsByEmailIncludeDeleted(@Param("email") String email);

    User findByEmail(String email);

    User findByLoginTypeAndProviderId(String loginType, String providerId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE User u SET u.refreshToken = :newRefreshToken WHERE u.email = :email")
    int updateRefreshTokenByEmail(@Param("email") String email, @Param("newRefreshToken") String newRefreshToken);

    @Modifying
    @Transactional
    @Query("update User u set u.refreshToken = null where u.email = :email")
    void clearRefreshToken(@Param("email") String email);

}
