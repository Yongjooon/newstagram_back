package com.ssafy.newstagram.rss.clustering.service;

import com.ssafy.newstagram.rss.clustering.repository.PeriodRecommendationRepository;
import com.ssafy.newstagram.rss.clustering.util.EmbeddingLiteralUtil;
import com.ssafy.newstagram.rss.mapper.ArticleMapper;
import com.ssafy.newstagram.rss.vo.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClusteringService {

    private final ArticleMapper articleMapper;
    private final PeriodRecommendationRepository periodRecommendationRepository;
    private final DbscanService dbscanService;

    public void clustering() {
        // 1) 임베딩 값 포함한 모든 기사 조회 (id + embedding)
        List<Article> articles = articleMapper.findAllWithEmbeddingLiteral();

        // 2) 임베딩 double[] 변환
        double[][] embeddings = new double[articles.size()][];
        for (int i = 0; i < articles.size(); i++) {
            String literal = articleMapper.findEmbeddingLiteralById(articles.get(i).getId());
            embeddings[i] = EmbeddingLiteralUtil.fromLiteral(literal);
        }

        // 3) DBSCAN 클러스터링 수행
        double eps = 0.4;
        int minPts = 3;
        int[] labels = dbscanService.clustering(embeddings, eps, minPts);

        // 4) 클러스터별 index 그룹핑
        Map<Integer, List<Integer>> clusterGroups = groupByCluster(labels);

        // 5) 클러스터별 중심 벡터 계산
        Map<Integer, double[]> representativeVectors = findClusterRepresentatives(embeddings, clusterGroups);

        // 6) 클러스터 정렬 및 저장
        saveClusterRankResult(embeddings, articles, clusterGroups, representativeVectors);
    }


    /**
     * 라벨 배열을 기반으로 클러스터별 인덱스 그룹핑
     */
    private Map<Integer, List<Integer>> groupByCluster(int[] labels) {
        Map<Integer, List<Integer>> clusterMap = new HashMap<>();

        for (int i = 0; i < labels.length; i++) {
            int label = labels[i];

            // noise는 스킵
            if (label == dbscanService.NOISE_INDEX) continue;

            clusterMap.computeIfAbsent(label, k -> new ArrayList<>()).add(i);
        }

        return clusterMap;
    }

    /**
     * 각 클러스터의 대표 벡터(Medoid) 찾기
     *
     * Medoid = 클러스터 내부에서 "다른 점들과의 거리 합이 최소"인 벡터
     */
    public Map<Integer, double[]> findClusterRepresentatives(
            double[][] embeddings,
            Map<Integer, List<Integer>> clusterGroups) {

        Map<Integer, double[]> representatives = new HashMap<>();

        for (Map.Entry<Integer, List<Integer>> entry : clusterGroups.entrySet()) {
            int clusterId = entry.getKey();
            List<Integer> indices = entry.getValue();

            double[] medoid = findMedoid(embeddings, indices);
            representatives.put(clusterId, medoid);
        }

        return representatives;
    }

    /**
     * 클러스터 내 벡터 중 가장 중심에 가까운 벡터(Medoid) 계산
     */
    private double[] findMedoid(double[][] embeddings, List<Integer> indices) {
        double minTotalDist = Double.MAX_VALUE;
        double[] bestVector = null;

        for (int i : indices) {
            double totalDist = 0.0;

            for (int j : indices) {
                if (i != j) {
                    totalDist += cosineDistance(embeddings[i], embeddings[j]);
                }
            }

            if (totalDist < minTotalDist) {
                minTotalDist = totalDist;
                bestVector = embeddings[i];
            }
        }

        return bestVector;
    }

    /**
     * cosine 거리 계산
     */
    public double cosineDistance(double[] a, double[] b) {
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

    /**
     * 클러스터 크기 → 클러스터 내부 중심 가까운 순으로 전체 데이터 랭킹 생성
     *
     * @param clusterGroups 클러스터별 index 모음
     * @param representatives 클러스터별 medoid 벡터
     */
    public void saveClusterRankResult(
            double[][] embeddings,
            List<Article> articles,
            Map<Integer, List<Integer>> clusterGroups,
            Map<Integer, double[]> representatives
    ) {
        int[] orderedClusterIds = new int[articles.size()];
        // 1) 클러스터 크기 기반 정렬 (내림차순)
        List<Map.Entry<Integer, List<Integer>>> sortedClusters = new ArrayList<>(clusterGroups.entrySet());
        sortedClusters.sort((a, b) -> b.getValue().size() - a.getValue().size());

        int orderClusterId = 1;  // 크기가 제일 큰 cluster의 id

        // 2) 클러스터 순서대로 내부 정렬 수행
        for (Map.Entry<Integer, List<Integer>> entry : sortedClusters) {
            int clusterId = entry.getKey();
            List<Integer> indices = entry.getValue();
            double[] medoid = representatives.get(clusterId);

            // ranking: 클러스터 크기 역순, score: 중심 벡터까지의 거리 순
            for (int idx : indices) {
                double distance = cosineDistance(embeddings[idx], medoid);
                periodRecommendationRepository.insertRankingAndScore("dbscan", orderClusterId, distance, articles.get(idx).getId());
            }
            orderClusterId++;
        }
    }

}
