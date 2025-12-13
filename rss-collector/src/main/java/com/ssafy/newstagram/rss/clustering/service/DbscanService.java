package com.ssafy.newstagram.rss.clustering.service;

import com.ssafy.newstagram.rss.clustering.util.CosineDistanceForDoubleArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.clustering.DBSCAN;

@Slf4j
@Service
public class DbscanService {

    public final int NOISE_INDEX = Integer.MAX_VALUE;

    /**
     * DBSCAN 알고리즘을 사용하여 임베딩 벡터 배열을 클러스터링한다.
     *
     * @param embeddings 클러스터링할 데이터 (기사 임베딩 벡터), 각 행이 하나의 데이터 포인트
     * @param radius DBSCAN 반경(eps). 같은 클러스터로 판단되는 최대 거리
     * @param minPts core point로 인정되기 위해 필요한 최소 이웃 개수
     * @return 각 데이터 포인트의 클러스터 라벨 배열
     *         - 클러스터 번호: 0, 1, 2, ...
     *         - noise: Integer.MAX_VALUE
     */
    public int[] clustering(double[][] embeddings, double radius, int minPts) {

        // fit(data, minPts, radius)를 사용 (순서 주의)
        DBSCAN<double[]> model = DBSCAN.fit(embeddings, new CosineDistanceForDoubleArray(), minPts, radius);

        return model.y;
    }


}
