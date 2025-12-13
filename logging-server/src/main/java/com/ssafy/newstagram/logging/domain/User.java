package com.ssafy.newstagram.logging.domain;

import com.ssafy.newstagram.logging.global.util.DoubleListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.util.List;

@Entity(name = "LoggingUser")
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    private Long id;

    @Column(name = "preference_embedding", columnDefinition = "vector")
    @Convert(converter = DoubleListConverter.class)
    @ColumnTransformer(write = "?::vector")
    private List<Double> preferenceEmbedding;
}
