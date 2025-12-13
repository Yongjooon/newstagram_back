package com.ssafy.newstagram.rss.clustering.util;

import smile.math.distance.Distance;

/**
 * double[]의 코사인 거리 계산
 */
public class CosineDistanceForDoubleArray implements Distance<double[]> {

    @Override
    public double d(double[] a, double[] b) {
        double dot = 0.0;
        double na = 0.0;
        double nb = 0.0;
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            double va = a[i];
            double vb = b[i];
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        if (na == 0 || nb == 0) {
            return 1.0;
        }
        double cos = dot / (Math.sqrt(na) * Math.sqrt(nb));
        return 1.0 - cos;
    }
}
