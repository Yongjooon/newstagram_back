package com.ssafy.newstagram.rss.clustering.util;

import java.util.Arrays;

public class EmbeddingLiteralUtil {

    // '[1.23, 4.56, ...]' → double[]
    public static double[] fromLiteral(String literal) {
        if (literal == null) return null;

        String trimmed = literal.replace("[", "").replace("]", "");
        String[] parts = trimmed.split(",");

        return Arrays.stream(parts)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
