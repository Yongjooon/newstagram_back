package com.ssafy.newstagram.logging.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Converter
@RequiredArgsConstructor
public class DoubleListConverter implements AttributeConverter<List<Double>, String> {

    private final ObjectMapper objectMapper;

    // 1. Java List -> DB String (저장할 때)
    // 예: [0.1, 0.5] -> "[0.1, 0.5]" (문자열로 변환)
    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null) return null;
        try {
            return new ObjectMapper().writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("[Util] DoubleListConverter - 벡터 데이터를 DB 포맷으로 변환 실패", e);
        }
    }

    // 2. DB String -> Java List (꺼낼 때)
    // 예: "[0.1, 0.5]" -> List<Double>
    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            Double[] array = new ObjectMapper().readValue(dbData, Double[].class);
            return Arrays.asList(array);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("[Util] DoubleListConverter - DB 벡터 데이터를 Java List로 변환 실패", e);
        }
    }
}
