package com.ssafy.newstagram.api.users.repository;

import com.ssafy.newstagram.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE email = :email", nativeQuery = true)
    boolean existsByEmailIncludeDeleted(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE phone_number = :phoneNumber", nativeQuery = true)
    boolean existsByPhoneNumberIncludedDeleted(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT u.id, u.phone_number, u.email, u.password_hash, u.nickname, u.login_type, u.provider_id, u.role, u.refresh_token, u.created_at, u.updated_at, NULL as preference_embedding FROM users u WHERE u.email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    @Override
    @Query(value = "SELECT u.id, u.phone_number, u.email, u.password_hash, u.nickname, u.login_type, u.provider_id, u.role, u.refresh_token, u.created_at, u.updated_at, NULL as preference_embedding FROM users u WHERE u.id = :id", nativeQuery = true)
    @NonNull
    Optional<User> findById(@NonNull @Param("id") Long id);

    @Query(value = "SELECT cast(preference_embedding as varchar) FROM users WHERE id = :userId", nativeQuery = true)
    String findPreferenceEmbeddingAsString(@Param("userId") Long userId);

    User findByLoginTypeAndProviderId(String loginType, String providerId);

    @Query(
            value = "SELECT u.id FROM users u WHERE u.phone_number = :phoneNumber",
            nativeQuery = true
    )
    Optional<Long> findIdByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
