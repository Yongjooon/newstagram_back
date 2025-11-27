package com.ssafy.newstagram.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(UserCategoryId.class)
public class UserCategory {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "category_id")
    private Long categoryId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
