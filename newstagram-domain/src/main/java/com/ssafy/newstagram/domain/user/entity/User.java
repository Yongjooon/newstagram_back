package com.ssafy.newstagram.domain.user.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@SoftDelete
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "login_type", nullable = false, length = 50)
    @Builder.Default
    private String loginType = "EMAIL";

    @Column(name = "provider_id")
    private String providerId;

    @Column(length = 50)
    @Builder.Default
    private String role = "USER";

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "preference_embedding", columnDefinition = "vector(1536)")
    private List<Double> preferenceEmbedding;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePasswordHash(String passwordHash){
        this.passwordHash = passwordHash;
    }
}
