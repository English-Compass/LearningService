package com.example.demo.service;

import com.example.demo.client.ProblemServiceClient;
import com.example.demo.dto.LearningCompletedEvent;
import com.example.demo.dto.analytics.LearningPatternAnalysisDTO;
import com.example.demo.dto.problem.SessionDataResponseDto;
import com.example.demo.entity.LearningSession;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.entity.LearningPatternAnalysis;
import com.example.demo.entity.LearningSessionEvent;
import com.example.demo.service.LearningPatternAnalysisService.LearningSessionResult;
import com.example.demo.repository.LearningPatternAnalysisRepository;
import com.example.demo.repository.LearningSessionRepository;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.repository.LearningSessionEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 학습 세션 완료 이벤트 리스너
 * 세션 완료 시 학습 결과를 분석하고 패턴 분석 결과를 데이터베이스에 저장
 * 분석 완료 후 다른 서비스들이 구독할 수 있는 이벤트를 발행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningSessionEventListener {

    private final ProblemServiceClient problemServiceClient;
    private final SessionDataMappingService sessionDataMappingService;
    private final LearningPatternAnalysisService patternAnalysisService;
    private final LearningAnalysisEventPublisher eventPublisher;
    private final LearningPatternAnalysisRepository analysisRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final LearningSessionEventRepository sessionEventRepository;
    private final ObjectMapper objectMapper;


    /**
     * 학습 세션 완료 이벤트 구독 및 처리
     * 세션 완료 시 즉시 호출되어 학습 패턴 분석을 수행
     */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleLearningSessionCompleted(LearningCompletedEvent event) {
        long startTime = System.currentTimeMillis();
        String sessionId = event.getSessionId();
        String userId = event.getUserId();
        
        try {
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("🚀 [분석 프로세스 시작] 학습 세션 완료 이벤트 처리 시작");
            log.info("   sessionId: {}", sessionId);
            log.info("   userId: {}", userId);
            log.info("   eventType: {}", event.getEventType());
            log.info("   sessionType: {}", event.getSessionType());
            log.info("   completedAt: {}", event.getCompletedAt());
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            log.info("┌─ [1단계] 이벤트 데이터에서 세션 정보 구성 (REST 호출 없음)");
            long apiStartTime = System.currentTimeMillis();
            SessionDataResponseDto sessionData = buildSessionDataFromEvent(event);
            long apiElapsedTime = System.currentTimeMillis() - apiStartTime;

            int questionCount = sessionData.getQuestions() != null ? sessionData.getQuestions().size() : 0;
            log.info("└─ ✅ 세션 데이터 구성 완료: questions={}개, 소요시간={}ms", questionCount, apiElapsedTime);
            
            log.info("┌─ [2단계] API 응답을 엔티티로 매핑");
            long mappingStartTime = System.currentTimeMillis();
            LearningSession session = sessionDataMappingService.mapToLearningSession(sessionData.getSession());
            String sessionType = session.getSessionType().name();
            List<QuestionAnswer> questionAnswers = sessionDataMappingService.mapToQuestionAnswers(
                sessionData.getQuestions(), sessionId, sessionType);
            List<LearningSessionEvent> sessionEvents = sessionDataMappingService.mapToLearningSessionEvents(
                sessionData.getEvents());
            long mappingElapsedTime = System.currentTimeMillis() - mappingStartTime;
            
            // 매핑된 데이터 상세 로그
            log.info("   ├─ 📋 매핑된 데이터:");
            log.info("   │  ├─ 세션: sessionId={}, sessionType={}, status={}", 
                session.getSessionId(), session.getSessionType(), session.getStatus());
            log.info("   │  ├─ 문제 답변: 총 {}개", questionAnswers.size());
            if (!questionAnswers.isEmpty()) {
                long correctCount = questionAnswers.stream().filter(QuestionAnswer::getIsCorrect).count();
                long totalTime = questionAnswers.stream()
                    .filter(qa -> qa.getTimeSpent() != null)
                    .mapToLong(QuestionAnswer::getTimeSpent)
                    .sum();
                log.info("   │  │  ├─ 정답: {}개, 오답: {}개", 
                    correctCount, questionAnswers.size() - correctCount);
                log.info("   │  │  └─ 총 소요시간: {}초", totalTime);
            }
            log.info("   │  └─ 세션 이벤트: {}개", sessionEvents.size());
            log.info("└─ ✅ 매핑 완료 (세션: 1개, 문제: {}개, 이벤트: {}개, 소요시간: {}ms)", 
                questionAnswers.size(), sessionEvents.size(), mappingElapsedTime);
            
            log.info("┌─ [3단계] 세션 결과 객체 생성");
            long buildStartTime = System.currentTimeMillis();
            LearningSessionResult sessionResult = buildSessionResult(session, sessionEvents, questionAnswers);
            long buildElapsedTime = System.currentTimeMillis() - buildStartTime;
            
            // 세션 결과 통계 상세 로그
            log.info("   ├─ 📊 세션 결과 통계:");
            log.info("   │  ├─ 전체 문제: {}개", sessionResult.getTotalQuestions());
            log.info("   │  ├─ 정답: {}개", sessionResult.getCorrectAnswers());
            log.info("   │  ├─ 오답: {}개", sessionResult.getTotalQuestions() - sessionResult.getCorrectAnswers());
            double accuracyRate = sessionResult.getTotalQuestions() > 0 
                ? (double) sessionResult.getCorrectAnswers() / sessionResult.getTotalQuestions() * 100 
                : 0.0;
            log.info("   │  ├─ 정답률: {:.2f}%", accuracyRate);
            log.info("   │  ├─ 총 소요시간: {}초", sessionResult.getTotalDuration());
            if (sessionResult.getTotalQuestions() > 0) {
                double avgTime = (double) sessionResult.getTotalDuration() / sessionResult.getTotalQuestions();
                log.info("   │  └─ 문제당 평균 시간: {:.2f}초", avgTime);
            }
            
            log.info("└─ ✅ 세션 결과 생성 완료 (전체문제: {}개, 정답: {}개, 총시간: {}초, 소요시간: {}ms)", 
                sessionResult.getTotalQuestions(), sessionResult.getCorrectAnswers(), 
                sessionResult.getTotalDuration(), buildElapsedTime);
            
            log.info("┌─ [4단계] 개별 세션 학습 패턴 분석");
            long sessionAnalysisStartTime = System.currentTimeMillis();
            LearningPatternAnalysisDTO sessionAnalysis = patternAnalysisService
                .performPatternAnalysis(sessionResult);
            long sessionAnalysisElapsedTime = System.currentTimeMillis() - sessionAnalysisStartTime;
            
            // 개별 세션 분석 결과 상세 로그
            log.info("   ├─ 📊 개별 세션 분석 결과:");
            log.info("   │  ├─ 전체 정답률: {:.2f}%", 
                sessionAnalysis.getOverallAccuracyRate() != null ? sessionAnalysis.getOverallAccuracyRate() : 0.0);
            log.info("   │  ├─ 평균 풀이 시간: {:.2f}초", 
                sessionAnalysis.getAverageSolvingTime() != null ? sessionAnalysis.getAverageSolvingTime() : 0.0);
            
            // 문제 유형별 성과
            if (sessionAnalysis.getQuestionTypePerformances() != null && !sessionAnalysis.getQuestionTypePerformances().isEmpty()) {
                log.info("   │  ├─ 문제 유형별 성과 ({}개 유형):", sessionAnalysis.getQuestionTypePerformances().size());
                sessionAnalysis.getQuestionTypePerformances().forEach(qtp -> 
                    log.info("   │  │  ├─ {}: 정답률 {:.2f}%, 문제수 {}개, 평균시간 {:.2f}초", 
                        qtp.getQuestionType(),
                        qtp.getAccuracyRate() != null ? qtp.getAccuracyRate() : 0.0,
                        qtp.getTotalQuestions() != null ? qtp.getTotalQuestions() : 0,
                        qtp.getAverageTime() != null ? qtp.getAverageTime() : 0.0));
            }
            
            // 복습/개선/강점 영역
            if (sessionAnalysis.getReviewRequiredTypes() != null && !sessionAnalysis.getReviewRequiredTypes().isEmpty()) {
                log.info("   │  ├─ 🔴 복습 필요 유형 (정답률 60% 미만): {}", sessionAnalysis.getReviewRequiredTypes());
            }
            if (sessionAnalysis.getImprovementRequiredTypes() != null && !sessionAnalysis.getImprovementRequiredTypes().isEmpty()) {
                log.info("   │  ├─ 🟡 개선 필요 유형 (정답률 60-80%): {}", sessionAnalysis.getImprovementRequiredTypes());
            }
            if (sessionAnalysis.getStrengthTypes() != null && !sessionAnalysis.getStrengthTypes().isEmpty()) {
                log.info("   │  ├─ 🟢 강점 영역 유형 (정답률 80% 이상): {}", sessionAnalysis.getStrengthTypes());
            }
            if (sessionAnalysis.getRecentWrongQuestionIds() != null && !sessionAnalysis.getRecentWrongQuestionIds().isEmpty()) {
                log.info("   │  ├─ 최근 오답 문제: {}개", sessionAnalysis.getRecentWrongQuestionIds().size());
            }
            if (sessionAnalysis.getSlowSolvingTypes() != null && !sessionAnalysis.getSlowSolvingTypes().isEmpty()) {
                log.info("   │  └─ 풀이 시간 긴 유형: {}", sessionAnalysis.getSlowSolvingTypes());
            }
            
            log.info("└─ ✅ 개별 세션 분석 완료 (정답률: {:.2f}%, 소요시간: {}ms)", 
                sessionAnalysis.getOverallAccuracyRate() != null ? sessionAnalysis.getOverallAccuracyRate() : 0.0, 
                sessionAnalysisElapsedTime);
            
            log.info("┌─ [5단계] 전체 학습 완료 분석 (최근 30일)");
            long completeAnalysisStartTime = System.currentTimeMillis();
            // 분석 기간 설정 (6단계에서도 사용)
            LocalDateTime analysisStartDate = LocalDateTime.now().minusDays(30);
            LocalDateTime analysisEndDate = LocalDateTime.now();
            LearningPatternAnalysisDTO completeAnalysis = patternAnalysisService
                .analyzeCompleteLearningIncremental(userId, analysisStartDate, analysisEndDate);
            long completeAnalysisElapsedTime = System.currentTimeMillis() - completeAnalysisStartTime;
            
            // 전체 학습 분석 결과 상세 로그
            log.info("   ├─ 📈 전체 학습 분석 결과 (기간: {} ~ {}):", 
                analysisStartDate.toLocalDate(), analysisEndDate.toLocalDate());
            log.info("   │  ├─ 전체 정답률: {:.2f}%", 
                completeAnalysis.getOverallAccuracyRate() != null ? completeAnalysis.getOverallAccuracyRate() : 0.0);
            log.info("   │  ├─ 평균 풀이 시간: {:.2f}초", 
                completeAnalysis.getAverageSolvingTime() != null ? completeAnalysis.getAverageSolvingTime() : 0.0);
            log.info("   │  ├─ 학습 빈도: {}", 
                completeAnalysis.getStudyFrequency() != null ? completeAnalysis.getStudyFrequency() : "N/A");
            log.info("   │  ├─ 선호 학습 시간: {}", 
                completeAnalysis.getPreferredStudyTime() != null ? completeAnalysis.getPreferredStudyTime() : "N/A");
            
            // 문제 유형별 성과
            if (completeAnalysis.getQuestionTypePerformances() != null && !completeAnalysis.getQuestionTypePerformances().isEmpty()) {
                log.info("   │  ├─ 문제 유형별 성과 ({}개 유형):", completeAnalysis.getQuestionTypePerformances().size());
                completeAnalysis.getQuestionTypePerformances().stream()
                    .limit(5)
                    .forEach(qtp -> 
                        log.info("   │  │  ├─ {}: 정답률 {:.2f}%, 문제수 {}개", 
                            qtp.getQuestionType(),
                            qtp.getAccuracyRate() != null ? qtp.getAccuracyRate() : 0.0,
                            qtp.getTotalQuestions() != null ? qtp.getTotalQuestions() : 0));
                if (completeAnalysis.getQuestionTypePerformances().size() > 5) {
                    log.info("   │  │  └─ ... 외 {}개 유형", completeAnalysis.getQuestionTypePerformances().size() - 5);
                }
            }
            
            // 복습/개선/강점 영역
            if (completeAnalysis.getReviewRequiredTypes() != null && !completeAnalysis.getReviewRequiredTypes().isEmpty()) {
                log.info("   │  ├─ 🔴 복습 필요 유형: {}", completeAnalysis.getReviewRequiredTypes());
            }
            if (completeAnalysis.getImprovementRequiredTypes() != null && !completeAnalysis.getImprovementRequiredTypes().isEmpty()) {
                log.info("   │  ├─ 🟡 개선 필요 유형: {}", completeAnalysis.getImprovementRequiredTypes());
            }
            if (completeAnalysis.getStrengthTypes() != null && !completeAnalysis.getStrengthTypes().isEmpty()) {
                log.info("   │  ├─ 🟢 강점 영역 유형: {}", completeAnalysis.getStrengthTypes());
            }
            if (completeAnalysis.getRecentWrongQuestionIds() != null && !completeAnalysis.getRecentWrongQuestionIds().isEmpty()) {
                log.info("   │  ├─ 최근 오답 문제: {}개", completeAnalysis.getRecentWrongQuestionIds().size());
            }
            if (completeAnalysis.getLongIntervalTypes() != null && !completeAnalysis.getLongIntervalTypes().isEmpty()) {
                log.info("   │  ├─ 학습 간격 긴 유형: {}", completeAnalysis.getLongIntervalTypes());
            }
            if (completeAnalysis.getSlowSolvingTypes() != null && !completeAnalysis.getSlowSolvingTypes().isEmpty()) {
                log.info("   │  └─ 풀이 시간 긴 유형: {}", completeAnalysis.getSlowSolvingTypes());
            }
            
            log.info("└─ ✅ 전체 학습 분석 완료 (정답률: {:.2f}%, 소요시간: {}ms)", 
                completeAnalysis.getOverallAccuracyRate() != null ? completeAnalysis.getOverallAccuracyRate() : 0.0, 
                completeAnalysisElapsedTime);
            
            log.info("┌─ [6단계] 데이터 및 분석 결과 저장");
            long saveStartTime = System.currentTimeMillis();
            
            // 6-1. 원본 데이터 저장 (향후 분석을 위한 이력 데이터)
            log.info("   ├─ 💾 세션 데이터 저장 중...");
            learningSessionRepository.save(session);
            log.info("   │  └─ ✅ 세션 저장 완료: sessionId={}", session.getSessionId());
            
            log.info("   ├─ 💾 답변 데이터 저장 중... ({}개)", questionAnswers.size());
            questionAnswerRepository.saveAll(questionAnswers);
            log.info("   │  └─ ✅ 답변 저장 완료: {}개", questionAnswers.size());
            
            if (!sessionEvents.isEmpty()) {
                log.info("   ├─ 💾 이벤트 데이터 저장 중... ({}개)", sessionEvents.size());
                sessionEventRepository.saveAll(sessionEvents);
                log.info("   │  └─ ✅ 이벤트 저장 완료: {}개", sessionEvents.size());
            }
            
            // 6-2. 분석 결과 저장
            log.info("   ├─ 💾 개별 세션 분석 결과 저장 중...");
            String sessionAnalysisId = saveSessionAnalysisResult(sessionAnalysis, session);
            log.info("   ├─ 💾 전체 학습 분석 결과 저장 중...");
            // analysisStartDate와 analysisEndDate는 5단계에서 이미 선언됨
            String completeAnalysisId = saveCompleteAnalysisResult(completeAnalysis, analysisStartDate, analysisEndDate);
            long saveElapsedTime = System.currentTimeMillis() - saveStartTime;
            log.info("   ├─ 저장된 분석 ID:");
            log.info("   │  ├─ 개별 세션 분석 ID: {}", sessionAnalysisId);
            log.info("   │  └─ 전체 학습 분석 ID: {}", completeAnalysisId);
            log.info("└─ ✅ 데이터 및 분석 결과 저장 완료 (개별분석ID: {}, 전체분석ID: {}, 소요시간: {}ms)", 
                sessionAnalysisId, completeAnalysisId, saveElapsedTime);
            
            log.info("┌─ [7단계] 분석 완료 이벤트 발행 (분석 데이터 포함)");
            long eventStartTime = System.currentTimeMillis();

            eventPublisher.publishWithAnalysisData(
                userId, sessionAnalysisId, completeAnalysisId, sessionId,
                completeAnalysis,
                sessionResult.getTotalDuration(),
                sessionResult.getTotalQuestions());

            long eventElapsedTime = System.currentTimeMillis() - eventStartTime;
            log.info("└─ ✅ 이벤트 발행 완료 (소요시간: {}ms)", eventElapsedTime);
            
            long totalElapsedTime = System.currentTimeMillis() - startTime;
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("🎉 [전체 완료] 학습 패턴 분석 및 이벤트 발행 완료");
            log.info("   sessionId={}, userId={}", sessionId, userId);
            log.info("   개별분석ID={}, 전체분석ID={}", sessionAnalysisId, completeAnalysisId);
            log.info("   총 소요시간: {}ms", totalElapsedTime);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
        } catch (Exception e) {
            long totalElapsedTime = System.currentTimeMillis() - startTime;
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("❌ [처리 실패] 학습 세션 완료 이벤트 처리 실패");
            log.error("   sessionId: {}", sessionId);
            log.error("   userId: {}", userId);
            log.error("   소요시간: {}ms", totalElapsedTime);
            log.error("   에러 타입: {}", e.getClass().getName());
            log.error("   에러 메시지: {}", e.getMessage());
            log.error("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("   스택 트레이스:");
            log.error("   ", e);
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 이벤트 처리 실패 시 재시도 이벤트 발행 또는 에러 로깅
            handleEventProcessingFailure(event, e);
            
            // 예외를 다시 던지지 않아서 트랜잭션이 롤백되지 않도록 함
            // (KafkaConsumer에서 이미 처리하므로)
        }
    }

    /**
     * Kafka 이벤트 데이터로 SessionDataResponseDto를 구성 (ProblemService REST 호출 대체)
     * 이벤트에 answers가 없으면 빈 DTO 반환 (분석은 데이터 없음 처리)
     */
    private SessionDataResponseDto buildSessionDataFromEvent(com.example.demo.dto.LearningCompletedEvent event) {
        SessionDataResponseDto.SessionDto sessionDto = SessionDataResponseDto.SessionDto.builder()
                .sessionId(event.getSessionId())
                .userId(event.getUserId())
                .sessionType(event.getSessionType() != null ? event.getSessionType().name() : null)
                .status("COMPLETED")
                .completedAt(event.getCompletedAt())
                .build();

        java.util.List<SessionDataResponseDto.QuestionAnswerDto> questionDtos =
                event.getAnswers() == null ? java.util.Collections.emptyList() :
                event.getAnswers().stream()
                        .map(a -> SessionDataResponseDto.QuestionAnswerDto.builder()
                                .questionId(a.getQuestionId())
                                .questionType(a.getQuestionType())
                                .majorCategory(a.getMajorCategory())
                                .minorCategory(a.getMinorCategory())
                                .difficultyLevel(a.getDifficultyLevel())
                                .userAnswer(a.getUserAnswer())
                                .isCorrect(a.getIsCorrect())
                                .timeSpent(a.getTimeSpent())
                                .answeredAt(a.getAnsweredAt())
                                .solveCount(a.getSolveCount())
                                .build())
                        .collect(java.util.stream.Collectors.toList());

        return SessionDataResponseDto.builder()
                .session(sessionDto)
                .questions(questionDtos)
                .events(java.util.Collections.emptyList())
                .build();
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
    private String saveSessionAnalysisResult(LearningPatternAnalysisDTO analysis, LearningSession session) {
        try {
            LocalDateTime analyzedAt = analysis.getAnalyzedAt() != null 
                ? analysis.getAnalyzedAt() 
                : LocalDateTime.now();
            
            // 세션의 시작일과 종료일 사용 (없으면 analyzedAt 사용)
            LocalDateTime startDate = session.getStartedAt() != null 
                ? session.getStartedAt() 
                : analyzedAt.minusHours(1); // 세션 시작일이 없으면 분석 시간 1시간 전
            LocalDateTime endDate = session.getCompletedAt() != null 
                ? session.getCompletedAt() 
                : analyzedAt; // 세션 종료일이 없으면 분석 시간
            
            // DTO를 엔티티로 변환 (기존 Entity 구조에 맞게)
            LearningPatternAnalysis entity = LearningPatternAnalysis.builder()
                // === 기본 분석 정보 ===
                .analysisType(analysis.getAnalysisType())                    // "SESSION_ANALYSIS" - 개별 세션 분석임을 구분
                .userId(analysis.getUserId())                               // 사용자 ID - 누구의 분석 결과인지 식별
                .sessionId(analysis.getSessionId())                         // 세션 ID - 어떤 학습 세션에 대한 분석인지 식별
                .analyzedAt(analyzedAt)                                     // 분석 수행 시간 - 언제 분석이 완료되었는지 기록
                .startDate(startDate)                                        // 세션 시작일
                .endDate(endDate)                                            // 세션 종료일
                
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
            log.info("   ├─ 엔티티 생성 완료: analysisType={}, userId={}, sessionId={}, startDate={}, endDate={}", 
                entity.getAnalysisType(), entity.getUserId(), entity.getSessionId(), 
                entity.getStartDate(), entity.getEndDate());
            
            log.info("   ├─ DB 저장 시도 중...");
            LearningPatternAnalysis savedAnalysis = analysisRepository.save(entity);
            log.info("   ├─ save() 메서드 호출 완료: analysisId={}", savedAnalysis.getAnalysisId());
            
            // 저장 후 즉시 flush하여 트랜잭션 커밋 전에 DB에 반영 확인
            analysisRepository.flush();
            log.info("   ├─ flush() 메서드 호출 완료");
            
            log.info("   ├─ DB 저장 완료: analysisId={}, userId={}", 
                savedAnalysis.getAnalysisId(), savedAnalysis.getUserId());
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
    private String saveCompleteAnalysisResult(LearningPatternAnalysisDTO analysis, 
                                             LocalDateTime startDate, 
                                             LocalDateTime endDate) {
        try {
            LocalDateTime analyzedAt = analysis.getAnalyzedAt() != null 
                ? analysis.getAnalyzedAt() 
                : LocalDateTime.now();
            
            LearningPatternAnalysis entity = LearningPatternAnalysis.builder()
                    // === 기본 분석 정보 ===
                    .analysisType("COMPLETE_ANALYSIS")                      // "COMPLETE_ANALYSIS" - 전체 학습 기간 분석임을 구분
                    .userId(analysis.getUserId())                           // 사용자 ID - 누구의 전체 학습 분석 결과인지 식별
                    .sessionId(null)                                        // null - 전체 분석이므로 특정 세션에 속하지 않음
                    .analyzedAt(analyzedAt)                                 // 분석 수행 시간 - 언제 전체 분석이 완료되었는지 기록
                    .startDate(startDate)                                   // 분석 기간 시작일
                    .endDate(endDate)                                       // 분석 기간 종료일
                    
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
            log.info("   ├─ 엔티티 생성 완료: analysisType={}, userId={}, sessionId={}, startDate={}, endDate={}", 
                entity.getAnalysisType(), entity.getUserId(), entity.getSessionId(), 
                entity.getStartDate(), entity.getEndDate());
            
            log.info("   ├─ DB 저장 시도 중...");
            LearningPatternAnalysis savedAnalysis = analysisRepository.save(entity);
            log.info("   ├─ save() 메서드 호출 완료: analysisId={}", savedAnalysis.getAnalysisId());
            
            // 저장 후 즉시 flush하여 트랜잭션 커밋 전에 DB에 반영 확인
            analysisRepository.flush();
            log.info("   ├─ flush() 메서드 호출 완료");
            
            log.info("   ├─ DB 저장 완료: analysisId={}, userId={}", 
                savedAnalysis.getAnalysisId(), savedAnalysis.getUserId());
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
