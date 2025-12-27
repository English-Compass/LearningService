package com.example.demo.service;

import com.example.demo.dto.AnalysisCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 학습 분석 이벤트 발행 서비스
 * 분석 완료 후 Kafka를 통해 다른 서비스들이 구독할 수 있는 이벤트를 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningAnalysisEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String ANALYSIS_COMPLETED_TOPIC = "learning-analysis-completed";

    /**
     * 개별 세션 분석 완료 이벤트 발행
     * Kafka를 통해 learning-analysis-completed 토픽으로 발행
     */
    public void publishSessionAnalysisCompletedEvent(String userId, String analysisId, String sessionId) {
        try {
            AnalysisCompletedEvent event = AnalysisCompletedEvent.builder()
                .eventType("ANALYSIS_COMPLETED")
                .userId(userId)
                .sessionId(sessionId)
                .sessionAnalysisId(analysisId)
                .completeAnalysisId(null)
                .completedAt(LocalDateTime.now())
                .metadata(Map.of(
                    "analysisType", "SESSION_ONLY"
                ))
                .build();

            kafkaTemplate.send(ANALYSIS_COMPLETED_TOPIC, userId, event);
            
            log.info("개별 세션 분석 완료 이벤트 발행: userId={}, analysisId={}, sessionId={}", 
                userId, analysisId, sessionId);
        } catch (Exception e) {
            log.error("개별 세션 분석 완료 이벤트 발행 실패: userId={}, sessionId={}", userId, sessionId, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }

    /**
     * 전체 학습 분석 완료 이벤트 발행
     * Kafka를 통해 learning-analysis-completed 토픽으로 발행
     */
    public void publishCompleteAnalysisCompletedEvent(String userId, String analysisId, String sessionId) {
        try {
            AnalysisCompletedEvent event = AnalysisCompletedEvent.builder()
                .eventType("ANALYSIS_COMPLETED")
                .userId(userId)
                .sessionId(sessionId)
                .sessionAnalysisId(null)
                .completeAnalysisId(analysisId)
                .completedAt(LocalDateTime.now())
                .metadata(Map.of(
                    "analysisType", "COMPLETE_ONLY"
                ))
                .build();

            kafkaTemplate.send(ANALYSIS_COMPLETED_TOPIC, userId, event);
            
            log.info("전체 학습 분석 완료 이벤트 발행: userId={}, analysisId={}, sessionId={}", 
                userId, analysisId, sessionId);
        } catch (Exception e) {
            log.error("전체 학습 분석 완료 이벤트 발행 실패: userId={}, sessionId={}", userId, sessionId, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }

    /**
     * 통합 분석 완료 이벤트 발행 (개별 세션 + 전체 학습)
     * Kafka를 통해 learning-analysis-completed 토픽으로 발행
     */
    public void publishIntegratedAnalysisCompletedEvent(String userId, String sessionAnalysisId, 
                                                       String completeAnalysisId, String sessionId,
                                                       Map<String, Object> additionalMetadata) {
        
        try {
            // 기본 메타데이터에 추가 메타데이터 병합
            Map<String, Object> metadata = new java.util.HashMap<>(Map.of(
                "sessionId", sessionId,
                "sessionAnalysisId", sessionAnalysisId,
                "completeAnalysisId", completeAnalysisId,
                "analysisType", "INTEGRATED_ANALYSIS"
            ));
            
            // 추가 메타데이터가 있으면 병합
            if (additionalMetadata != null && !additionalMetadata.isEmpty()) {
                metadata.putAll(additionalMetadata);
            }

            // Kafka로 발행할 이벤트 생성
            AnalysisCompletedEvent event = AnalysisCompletedEvent.builder()
                .eventType("ANALYSIS_COMPLETED")
                .userId(userId)
                .sessionId(sessionId)
                .sessionAnalysisId(sessionAnalysisId)
                .completeAnalysisId(completeAnalysisId)
                .completedAt(LocalDateTime.now())
                .metadata(metadata)
                .build();

            // Kafka로 이벤트 발행 (userId를 key로 사용하여 같은 사용자의 이벤트가 같은 파티션으로 가도록)
            kafkaTemplate.send(ANALYSIS_COMPLETED_TOPIC, userId, event);
            
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📤 [Kafka 이벤트 발행] 분석 완료 이벤트 발행 완료");
            log.info("   토픽: {}", ANALYSIS_COMPLETED_TOPIC);
            log.info("   userId={}, sessionId={}", userId, sessionId);
            log.info("   sessionAnalysisId={}, completeAnalysisId={}", sessionAnalysisId, completeAnalysisId);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            log.error("분석 완료 이벤트 발행 실패: userId={}, sessionId={}, error={}", 
                userId, sessionId, e.getMessage(), e);
            throw new RuntimeException("분석 완료 이벤트 발행 실패", e);
        }
    }

    /**
     * 분석 실패 이벤트 발행
     * Kafka를 통해 learning-analysis-completed 토픽으로 발행
     */
    public void publishAnalysisFailedEvent(String userId, String sessionId, String errorMessage, String analysisType) {
        try {
            AnalysisCompletedEvent event = AnalysisCompletedEvent.builder()
                .eventType("ANALYSIS_FAILED")
                .userId(userId)
                .sessionId(sessionId)
                .sessionAnalysisId(null)
                .completeAnalysisId(null)
                .completedAt(LocalDateTime.now())
                .metadata(Map.of(
                    "errorMessage", errorMessage,
                    "failedAnalysisType", analysisType,
                    "status", "FAILED"
                ))
                .build();

            kafkaTemplate.send(ANALYSIS_COMPLETED_TOPIC, userId, event);
            
            log.warn("분석 실패 이벤트 발행: userId={}, sessionId={}, error={}, type={}", 
                userId, sessionId, errorMessage, analysisType);
        } catch (Exception e) {
            log.error("분석 실패 이벤트 발행 실패: userId={}, sessionId={}", userId, sessionId, e);
        }
    }
}
