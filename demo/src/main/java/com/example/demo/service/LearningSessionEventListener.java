package com.example.demo.service;

import com.example.demo.dto.LearningCompletedEvent;
import com.example.demo.dto.analytics.LearningPatternAnalysisDTO;
import com.example.demo.dto.analytics.QuestionTypePerformance;
import com.example.demo.entity.LearningSession;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.entity.LearningPatternAnalysis;
import com.example.demo.entity.LearningSessionEvent;
import com.example.demo.repository.LearningSessionRepository;
import com.example.demo.repository.LearningSessionEventRepository;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.service.LearningPatternAnalysisService.LearningSessionResult;
import com.example.demo.repository.LearningPatternAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 학습 세션 완료 이벤트 리스너
 * 세션 완료 시 학습 결과를 분석하고 패턴 분석 결과를 데이터베이스에 저장
 * 분석 완료 후 다른 서비스들이 구독할 수 있는 이벤트를 발행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningSessionEventListener {

    private final LearningSessionRepository sessionRepository;
    private final LearningSessionEventRepository eventRepository;
    private final QuestionAnswerRepository answerRepository;
    private final LearningPatternAnalysisService patternAnalysisService;
    private final LearningAnalysisEventPublisher eventPublisher;
    private final LearningPatternAnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;


    /**
     * 학습 세션 완료 이벤트 구독 및 처리
     * 세션 완료 시 즉시 호출되어 학습 패턴 분석을 수행
     */
    @EventListener
    @Transactional
    public void handleLearningSessionCompleted(LearningCompletedEvent event) {
        try {
            String sessionId = event.getSessionId();
            String userId = event.getUserId();
            
            log.info("학습 세션 완료 이벤트 수신: sessionId={}, userId={}", sessionId, userId);
            
            // 1. 세션 기본 정보 데이터베이스에서 조회
            LearningSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
            
            // 2. 세션 이벤트 히스토리 조회 (Optional을 List로 변환)
            List<LearningSessionEvent> sessionEvents = eventRepository
                .findBySessionId(sessionId)
                .map(List::of)
                .orElse(List.of());
            
            // 3. 문제 답변 기록 조회 (question_answer 테이블에서 실제 데이터 조회)
            List<QuestionAnswer> questionAnswers = answerRepository
                .findBySessionIdOrderByAnsweredAtAsc(sessionId);
            
            // 4. 통합된 세션 결과 객체 생성 (DB에서 조회한 데이터로)
            LearningSessionResult sessionResult = buildSessionResult(session, sessionEvents, questionAnswers);
            
            // 5. 개별 세션 학습 패턴 분석 수행 (즉시 피드백용)
            LearningPatternAnalysisDTO sessionAnalysis = patternAnalysisService
                .performPatternAnalysis(sessionResult);
            
            // 6. 전체 학습 완료 분석 수행 (증분 분석으로 성능 향상)
            LearningPatternAnalysisDTO completeAnalysis = patternAnalysisService
                .analyzeCompleteLearningIncremental(userId, LocalDateTime.now().minusDays(30), LocalDateTime.now());
            
            // 7. 두 분석 결과 모두 데이터베이스에 저장
            String sessionAnalysisId = saveSessionAnalysisResult(sessionAnalysis);
            String completeAnalysisId = saveCompleteAnalysisResult(completeAnalysis);
            
            // 8. 통합 분석 완료 이벤트 발행 (다른 서비스들이 구독할 수 있도록)
            Map<String, Object> additionalMetadata = Map.of(
                "totalQuestions", sessionResult.getTotalQuestions(),
                "correctAnswers", sessionResult.getCorrectAnswers(),
                "accuracyRate", calculateAccuracyRate(sessionResult.getCorrectAnswers(), sessionResult.getTotalQuestions()),
                "totalTimeSpent", sessionResult.getTotalDuration(),
                "averageTimePerQuestion", calculateAverageTimePerQuestion(sessionResult.getTotalDuration(), sessionResult.getTotalQuestions())
            );
            
            eventPublisher.publishIntegratedAnalysisCompletedEvent(
                userId, sessionAnalysisId, completeAnalysisId, sessionId, additionalMetadata);
            
            log.info("학습 패턴 분석 완료 및 이벤트 발행: userId={}, sessionAnalysisId={}, completeAnalysisId={}, sessionId={}", 
                userId, sessionAnalysisId, completeAnalysisId, sessionId);
                
        } catch (Exception e) {
            log.error("학습 세션 완료 이벤트 처리 실패: sessionId={}, userId={}", 
                event.getSessionId(), event.getUserId(), e);
            
            // 이벤트 처리 실패 시 재시도 이벤트 발행 또는 에러 로깅
            handleEventProcessingFailure(event, e);
        }
    }

    /**
     * 세션 결과 객체 생성 (DB에서 조회한 데이터 기반)
     */
    private LearningSessionResult buildSessionResult(LearningSession session, 
                                                   List<LearningSessionEvent> sessionEvents, 
                                                   List<QuestionAnswer> questionAnswers) {
        
        // 기본 통계 계산 (DB 데이터 기반)
        int totalQuestions = questionAnswers.size();
        int correctAnswers = (int) questionAnswers.stream()
            .filter(QuestionAnswer::getIsCorrect).count();
        
        // 총 학습 시간 계산 (초 단위, DB 데이터 기반)
        long totalTimeSpent = questionAnswers.stream()
            .filter(answer -> answer.getTimeSpent() != null)
            .mapToLong(QuestionAnswer::getTimeSpent)
            .sum();
        
        return LearningSessionResult.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .totalQuestions(totalQuestions)
            .correctAnswers(correctAnswers)
            .totalDuration(totalTimeSpent)
            .questionAnswers(questionAnswers)
            .build();
    }

    /**
     * 정답률 계산 (DB 데이터 기반)
     */
    private double calculateAccuracyRate(int correctAnswers, int totalQuestions) {
        if (totalQuestions == 0) return 0.0;
        return (double) correctAnswers / totalQuestions * 100;
    }

    /**
     * 문제당 평균 소요시간 계산 (DB 데이터 기반)
     */
    private double calculateAverageTimePerQuestion(long totalDuration, int totalQuestions) {
        if (totalQuestions == 0) return 0.0;
        return (double) totalDuration / totalQuestions;
    }

    /**
     * 개별 세션 분석 결과 데이터베이스에 저장
     */
    private String saveSessionAnalysisResult(LearningPatternAnalysisDTO analysis) {
        try {
            // DTO를 엔티티로 변환 (기존 Entity 구조에 맞게)
            LearningPatternAnalysis entity = LearningPatternAnalysis.builder()
                // === 기본 분석 정보 ===
                .analysisType(analysis.getAnalysisType())                    // "SESSION_ANALYSIS" - 개별 세션 분석임을 구분
                .userId(analysis.getUserId())                               // 사용자 ID - 누구의 분석 결과인지 식별
                .sessionId(analysis.getSessionId())                         // 세션 ID - 어떤 학습 세션에 대한 분석인지 식별
                .analyzedAt(analysis.getAnalyzedAt())                       // 분석 수행 시간 - 언제 분석이 완료되었는지 기록
                
                // === 기존 Entity 필드에 JSON 형태로 저장 ===
                .questionTypePerformances(convertListToJson(analysis.getQuestionTypePerformances()))  // 문제 유형별 성과 통계 (JSON)
                
                // performanceAnalysis 필드에 모든 분석 데이터를 통합해서 저장
                .performanceAnalysis(convertObjectToJson(Map.of(
                    "reviewRequiredTypes", analysis.getReviewRequiredTypes(),
                    "improvementRequiredTypes", analysis.getImprovementRequiredTypes(),
                    "strengthTypes", analysis.getStrengthTypes(),
                    "recentWrongQuestionIds", analysis.getRecentWrongQuestionIds(),
                    "longIntervalTypes", analysis.getLongIntervalTypes(),
                    "slowSolvingTypes", analysis.getSlowSolvingTypes(),
                    "overallAccuracyRate", analysis.getOverallAccuracyRate(),
                    "averageSolvingTime", analysis.getAverageSolvingTime(),
                    "studyFrequency", analysis.getStudyFrequency(),
                    "preferredStudyTime", analysis.getPreferredStudyTime()
                )))
                .build();

            // 데이터베이스에 저장
            LearningPatternAnalysis savedAnalysis = analysisRepository.save(entity);
            
            log.info("개별 세션 학습 패턴 분석 결과 저장 완료: analysisId={}, userId={}", 
                savedAnalysis.getAnalysisId(), savedAnalysis.getUserId());
            
            return savedAnalysis.getAnalysisId().toString();
            
        } catch (Exception e) {
            log.error("개별 세션 분석 결과 저장 실패: userId={}, error={}", 
                analysis.getUserId(), e.getMessage(), e);
            throw new RuntimeException("분석 결과 저장 실패", e);
        }
    }

    /**
     * 전체 학습 완료 분석 결과 데이터베이스에 저장
     * 개별 세션 분석과 동일한 엔티티에 저장하되 analysisType으로 구분
     */
    private String saveCompleteAnalysisResult(LearningPatternAnalysisDTO analysis) {
        try {
            LearningPatternAnalysis entity = LearningPatternAnalysis.builder()
                    // === 기본 분석 정보 ===
                    .analysisType("COMPLETE_ANALYSIS")                      // "COMPLETE_ANALYSIS" - 전체 학습 기간 분석임을 구분
                    .userId(analysis.getUserId())                           // 사용자 ID - 누구의 전체 학습 분석 결과인지 식별
                    .sessionId(null)                                        // null - 전체 분석이므로 특정 세션에 속하지 않음
                    .analyzedAt(analysis.getAnalyzedAt())                   // 분석 수행 시간 - 언제 전체 분석이 완료되었는지 기록
                    
                    // === 기존 Entity 필드에 JSON 형태로 저장 ===
                    .questionTypePerformances(convertListToJson(analysis.getQuestionTypePerformances()))  // 전체 기간 문제 유형별 성과 통계 (JSON)
                    
                    // performanceAnalysis 필드에 모든 분석 데이터를 통합해서 저장
                    .performanceAnalysis(convertObjectToJson(Map.of(
                        "reviewRequiredTypes", analysis.getReviewRequiredTypes(),
                        "improvementRequiredTypes", analysis.getImprovementRequiredTypes(),
                        "strengthTypes", analysis.getStrengthTypes(),
                        "recentWrongQuestionIds", analysis.getRecentWrongQuestionIds(),
                        "longIntervalTypes", analysis.getLongIntervalTypes(),
                        "slowSolvingTypes", analysis.getSlowSolvingTypes(),
                        "overallAccuracyRate", analysis.getOverallAccuracyRate(),
                        "averageSolvingTime", analysis.getAverageSolvingTime(),
                        "studyFrequency", analysis.getStudyFrequency(),
                        "preferredStudyTime", analysis.getPreferredStudyTime()
                    )))
                    .build();

            // 데이터베이스에 저장
            LearningPatternAnalysis savedAnalysis = analysisRepository.save(entity);
            
            log.info("전체 학습 완료 분석 결과 저장 완료: analysisId={}, userId={}", 
                savedAnalysis.getAnalysisId(), savedAnalysis.getUserId());
            
            return savedAnalysis.getAnalysisId().toString();
            
        } catch (Exception e) {
            log.error("전체 학습 완료 분석 결과 저장 실패: userId={}, error={}", 
                analysis.getUserId(), e.getMessage(), e);
            throw new RuntimeException("분석 결과 저장 실패", e);
        }
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    private String convertObjectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON 변환 실패: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 리스트를 JSON 문자열로 변환
     */
    private String convertListToJson(List<?> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("JSON 변환 실패: {}", e.getMessage());
            return "[]";
        }
    }

    /**
     * 이벤트 처리 실패 시 처리 로직
     */
    private void handleEventProcessingFailure(LearningCompletedEvent event, Exception e) {
        log.error("이벤트 처리 실패로 인한 재시도 이벤트 발행: sessionId={}, userId={}", 
            event.getSessionId(), event.getUserId());
        
        // TODO: 재시도 이벤트 발행 또는 에러 처리 로직 구현
        // 현재는 로깅만 수행
    }

    /**
     * 학습 패턴 분석 완료 이벤트 발행을 위한 DTO
     * 다른 서비스들이 구독할 수 있는 이벤트 데이터 구조
     */
    public static class LearningPatternAnalysisCompletedEvent {
        private String eventType = "PATTERN_ANALYSIS_COMPLETED";
        private String userId;
        private String sessionId;
        private String sessionAnalysisId;
        private String completeAnalysisId;
        private LocalDateTime completedAt;
        private Map<String, Object> additionalMetadata;

        public static LearningPatternAnalysisCompletedEventBuilder builder() {
            return new LearningPatternAnalysisCompletedEventBuilder();
        }

        public static class LearningPatternAnalysisCompletedEventBuilder {
            private LearningPatternAnalysisCompletedEvent event = new LearningPatternAnalysisCompletedEvent();

            public LearningPatternAnalysisCompletedEventBuilder userId(String userId) {
                event.userId = userId;
                return this;
            }

            public LearningPatternAnalysisCompletedEventBuilder sessionId(String sessionId) {
                event.sessionId = sessionId;
                return this;
            }

            public LearningPatternAnalysisCompletedEventBuilder sessionAnalysisId(String sessionAnalysisId) {
                event.sessionAnalysisId = sessionAnalysisId;
                return this;
            }

            public LearningPatternAnalysisCompletedEventBuilder completeAnalysisId(String completeAnalysisId) {
                event.completeAnalysisId = completeAnalysisId;
                return this;
            }

            public LearningPatternAnalysisCompletedEventBuilder completedAt(LocalDateTime completedAt) {
                event.completedAt = completedAt;
                return this;
            }

            public LearningPatternAnalysisCompletedEventBuilder additionalMetadata(Map<String, Object> additionalMetadata) {
                event.additionalMetadata = additionalMetadata;
                return this;
            }

            public LearningPatternAnalysisCompletedEvent build() {
                return event;
            }
        }

        // Getter 메서드들
        public String getEventType() { return eventType; }
        public String getUserId() { return userId; }
        public String getSessionId() { return sessionId; }
        public String getSessionAnalysisId() { return sessionAnalysisId; }
        public String getCompleteAnalysisId() { return completeAnalysisId; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public Map<String, Object> getAdditionalMetadata() { return additionalMetadata; }
    }
}
