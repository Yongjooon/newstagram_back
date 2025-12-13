package com.ssafy.newstagram.logging.feature.articleclick;

import com.ssafy.newstagram.logging.domain.*;
import com.ssafy.newstagram.logging.domain.repository.ArticleRepository;
import com.ssafy.newstagram.logging.domain.repository.UserInteractionLogRepository;
import com.ssafy.newstagram.logging.domain.repository.UserRepository;
import com.ssafy.newstagram.logging.global.util.VectorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserInteractionLogRepository logRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateUserPreference(Long userId) {

        // 1. 최근 로그 조회
        Pageable limit = PageRequest.of(0, 30, Sort.by("createdAt").descending());
        List<UserInteractionLog> logs = logRepository.findByUserId(userId, limit);
        if (logs.isEmpty()) return;

        // 합계 벡터
        List<Double> weightedSum = VectorUtils.createZeroVector(1536);
        // 가중치 총합
        double totalWeight = 0.0;

        for (UserInteractionLog logData : logs) {
            // 2. 해당 로그의 기사 벡터 조회
            Article article = articleRepository.findById(logData.getArticleId()).orElse(null);
            if (article == null || article.getEmbedding() == null) continue;

            // 3. 시간 가중치 (Time Decay) 계산
            double hoursDiff = ChronoUnit.HOURS.between(logData.getCreatedAt(), LocalDateTime.now());
            double weight = Math.exp(-0.02 * Math.abs(hoursDiff));

            // 4. 벡터 누적: (기사벡터 * 가중치)를 합계에 더함
            List<Double> weightedVector = VectorUtils.multiply(article.getEmbedding(), weight);
            weightedSum = VectorUtils.add(weightedSum, weightedVector);
            totalWeight += weight;
        }

        // 5. 가중 평균 계산 및 업데이트
        if (totalWeight > 0) {
            List<Double> finalVector = VectorUtils.divide(weightedSum, totalWeight);

            User user = userRepository.findById(userId).orElseThrow();
            user.setPreferenceEmbedding(finalVector);
            log.info("[Kafka] UserPreferenceService - User {} 취향 벡터 업데이트 완료 (참조 로그 수: {})", userId, logs.size());
        }
    }
}