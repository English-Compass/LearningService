package com.example.demo.service;

import com.example.demo.dto.AnalysisCompletedEvent;
import com.example.demo.dto.analytics.LearningPatternAnalysisDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 학습 분석 이벤트 발행 서비스
 * 핵심 데이터(약점 유형, 오답 ID, 학습 패턴)만 포함한 컴팩트 이벤트 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningAnalysisEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ANALYSIS_COMPLETED_TOPIC = "learning-analysis-completed";

    /**
     * 분석 완료 이벤트 발행 (ProblemService 문제 할당용)
     * 정답률·학습시간 등 프론트가 계산 가능한 지표는 제외
     */
    public void publishWithAnalysisData(String userId, String sessionAnalysisId,
                                        String completeAnalysisId, String sessionId,
                                        LearningPatternAnalysisDTO completeAnalysis,
                                        long totalDuration, int totalQuestions) {
        try {
            List<String> weakTypes = completeAnalysis.getReviewRequiredTypes() != null
                    ? completeAnalysis.getReviewRequiredTypes() : new ArrayList<>();
            List<String> wrongIds = completeAnalysis.getRecentWrongQuestionIds() != null
                    ? completeAnalysis.getRecentWrongQuestionIds() : new ArrayList<>();

            double accuracy = completeAnalysis.getOverallAccuracyRate() != null
                    ? completeAnalysis.getOverallAccuracyRate() : 0.0;
            String pattern = accuracy >= 80.0 ? "IMPROVING" : accuracy >= 60.0 ? "STABLE" : "STRUGGLING";

            AnalysisCompletedEvent event = AnalysisCompletedEvent.builder()
                    .eventType("ANALYSIS_COMPLETED")
                    .userId(userId)
                    .sessionId(sessionId)
                    .weakQuestionTypes(weakTypes)
                    .wrongQuestionIds(wrongIds)
                    .recommendedReviewQuestions(wrongIds)
                    .learningPattern(pattern)
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(ANALYSIS_COMPLETED_TOPIC, userId, event);

            log.info("분석 이벤트 발행: userId={}, sessionId={}, pattern={}, weakTypes={}, wrongIds={}개",
                    userId, sessionId, pattern, weakTypes, wrongIds.size());
        } catch (Exception e) {
            log.error("분석 이벤트 발행 실패: userId={}, sessionId={}", userId, sessionId, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }
}
