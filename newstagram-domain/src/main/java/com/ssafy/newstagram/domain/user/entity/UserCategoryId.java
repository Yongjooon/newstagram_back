package com.ssafy.newstagram.domain.user.entity;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryId implements Serializable {
    private Long userId;
    private Long categoryId;
}
