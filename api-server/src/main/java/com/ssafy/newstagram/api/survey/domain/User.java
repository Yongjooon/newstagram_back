package com.ssafy.newstagram.api.survey.domain;

import com.ssafy.newstagram.api.survey.util.DoubleListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import java.util.List;

@Entity(name = "SurveyUser")
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
