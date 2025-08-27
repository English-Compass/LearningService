package com.example.demo.service;

import com.example.demo.dto.analytics.LearningPatternAnalysisDTO;
import com.example.demo.service.LearningSessionEventListener.LearningPatternAnalysisCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 학습 분석 완료 이벤트 발행 서비스
 * 분석 완료 시 다른 서비스들이 구독할 수 있는 이벤트를 발행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningAnalysisEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 개별 세션 분석 완료 이벤트 발행
     */
    public void publishSessionAnalysisCompletedEvent(String userId, String analysisId, String sessionId) {
        LearningPatternAnalysisCompletedEvent event = LearningPatternAnalysisCompletedEvent.builder()
            .userId(userId)
            .sessionAnalysisId(analysisId)
            .completeAnalysisId(null) // 개별 세션만 분석된 경우
            .completedAt(LocalDateTime.now())
            .additionalMetadata(Map.of(
                "sessionId", sessionId,
                "analysisId", analysisId,
                "analysisType", "SESSION_ONLY"
            ))
            .build();

        eventPublisher.publishEvent(event);
        
        log.info("개별 세션 분석 완료 이벤트 발행: userId={}, analysisId={}, sessionId={}", 
            userId, analysisId, sessionId);
    }

    /**
     * 전체 학습 분석 완료 이벤트 발행
     */
    public void publishCompleteAnalysisCompletedEvent(String userId, String analysisId, String sessionId) {
        LearningPatternAnalysisCompletedEvent event = LearningPatternAnalysisCompletedEvent.builder()
            .userId(userId)
            .sessionAnalysisId(null) // 전체 학습만 분석된 경우
            .completeAnalysisId(analysisId)
            .completedAt(LocalDateTime.now())
            .additionalMetadata(Map.of(
                "sessionId", sessionId,
                "analysisId", analysisId,
                "analysisType", "COMPLETE_ONLY"
            ))
            .build();

        eventPublisher.publishEvent(event);
        
        log.info("전체 학습 분석 완료 이벤트 발행: userId={}, analysisId={}, sessionId={}", 
            userId, analysisId, sessionId);
    }

    /**
     * 통합 분석 완료 이벤트 발행 (개별 세션 + 전체 학습)
     */
    public void publishIntegratedAnalysisCompletedEvent(String userId, String sessionAnalysisId, 
                                                       String completeAnalysisId, String sessionId,
                                                       Map<String, Object> additionalMetadata) {
        
        // 기본 메타데이터에 추가 메타데이터 병합
        Map<String, Object> metadata = Map.of(
            "sessionId", sessionId,
            "sessionAnalysisId", sessionAnalysisId,
            "completeAnalysisId", completeAnalysisId,
            "analysisType", "INTEGRATED_ANALYSIS"
        );
        
        // 추가 메타데이터가 있으면 병합
        if (additionalMetadata != null && !additionalMetadata.isEmpty()) {
            // Java 9+ Map.of는 불변 맵이므로 새로운 맵 생성
            Map<String, Object> mergedMetadata = new java.util.HashMap<>(metadata);
            mergedMetadata.putAll(additionalMetadata);
            metadata = mergedMetadata;
        }

        LearningPatternAnalysisCompletedEvent event = LearningPatternAnalysisCompletedEvent.builder()
            .userId(userId)
            .sessionAnalysisId(sessionAnalysisId)
            .completeAnalysisId(completeAnalysisId)
            .completedAt(LocalDateTime.now())
            .additionalMetadata(metadata)
            .build();

        eventPublisher.publishEvent(event);
        
        log.info("통합 분석 완료 이벤트 발행: userId={}, sessionAnalysisId={}, completeAnalysisId={}, sessionId={}", 
            userId, sessionAnalysisId, completeAnalysisId, sessionId);
    }

    /**
     * 분석 실패 이벤트 발행
     */
    public void publishAnalysisFailedEvent(String userId, String sessionId, String errorMessage, String analysisType) {
        LearningPatternAnalysisCompletedEvent event = LearningPatternAnalysisCompletedEvent.builder()
            .userId(userId)
            .sessionAnalysisId(null)
            .completeAnalysisId(null)
            .completedAt(LocalDateTime.now())
            .additionalMetadata(Map.of(
                "sessionId", sessionId,
                "errorMessage", errorMessage,
                "failedAnalysisType", analysisType,
                "status", "FAILED"
            ))
            .build();

        eventPublisher.publishEvent(event);
        
        log.warn("분석 실패 이벤트 발행: userId={}, sessionId={}, error={}, type={}", 
            userId, sessionId, errorMessage, analysisType);
    }
}
