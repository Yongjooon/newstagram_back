package com.ssafy.newstagram.api.users.repository;

import com.ssafy.newstagram.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE email = :email", nativeQuery = true)
    boolean existsByEmailIncludeDeleted(@Param("email") String email);

    Optional<User> findByEmail(String email);

    User findByLoginTypeAndProviderId(String loginType, String providerId);

}
