package com.ssafy.newstagram.logging.global.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VectorUtils {
    // 0으로 채워진 벡터 생성
    public static List<Double> createZeroVector(int size) {
        return new ArrayList<>(Collections.nCopies(size, 0.0));
    }

    // 벡터 * 가중치
    public static List<Double> multiply(List<Double> vector, double weight) {
        return vector.stream()
                .map(val -> val * weight)
                .collect(Collectors.toList());
    }

    // 벡터 + 벡터
    public static List<Double> add(List<Double> v1, List<Double> v2) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < v1.size(); i++) {
            result.add(v1.get(i) + v2.get(i));
        }
        return result;
    }

    // 벡터 / 값
    public static List<Double> divide(List<Double> vector, double divisor) {
        if (divisor == 0) return vector;
        return vector.stream().map(v -> v / divisor).collect(Collectors.toList());
    }
}
