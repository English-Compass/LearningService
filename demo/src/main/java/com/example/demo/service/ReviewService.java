package com.example.demo.service;

import com.example.demo.dto.LearningSessionDto;
import com.example.demo.service.LearningServiceOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 오답/복습 문제 제공 서비스
 * 기본 10문제 + 오답 문제(취약 영역) 제공
 * 복습 완료 시 정답 처리된 문제는 다음 복습에서 제외
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final LearningServiceOrchestrator learningServiceOrchestrator;

    /**
     * 학습 완료 분석 결과를 바탕으로 개인화된 복습 문제 세트 생성
     * 기본 10문제 + 오답 문제(취약 영역)
     */
    public void createPersonalizedReviewSet(LearningPatternAnalysisService.CompleteLearningAnalysis analysis) {
        try {
            log.info("개인화된 복습 문제 세트 생성: userId={}, sessionId={}", 
                analysis.getUserId(), analysis.getSessionId());
            
            // 1. 기본 10문제 + 오답 문제(취약 영역) 세트 생성
            List<ReviewQuestionSet> reviewSets = generateBasicPlusWrongAnswerSets(analysis);
            
            // 2. 자동으로 복습 세션 생성
            automaticallyCreateReviewSessions(analysis.getUserId(), reviewSets);
            
            log.info("개인화된 복습 문제 세트 생성 완료: userId={}, sessionId={}, reviewSets={}", 
                analysis.getUserId(), analysis.getSessionId(), reviewSets.size());
                
        } catch (Exception e) {
            log.error("개인화된 복습 문제 세트 생성 실패: userId={}, sessionId={}", 
                analysis.getUserId(), analysis.getSessionId(), e);
        }
    }

    /**
     * 기본 10문제 + 오답 문제(취약 영역) 세트 생성
     * 총 10문제 = 오답 문제 + 푼 문제
     */
    private List<ReviewQuestionSet> generateBasicPlusWrongAnswerSets(LearningPatternAnalysisService.CompleteLearningAnalysis analysis) {
        List<String> weakQuestionTypes = analysis.getWeakQuestionTypes();
        List<String> wrongQuestionIds = analysis.getWrongQuestionIds();
        String overallPattern = analysis.getOverallLearningPattern();
        double consistencyScore = analysis.getConsistencyScore();
        
        return weakQuestionTypes.stream()
            .map(questionType -> {
                // 총 문제 수는 10문제로 고정
                int totalQuestions = 10;
                
                // 오답 문제 수 계산 (취약 영역 기반)
                int wrongAnswerQuestions = calculateWrongAnswerQuestions(questionType, analysis, overallPattern);
                
                // 푼 문제 수 = 총 10문제 - 오답 문제 수
                int solvedQuestions = totalQuestions - wrongAnswerQuestions;
                
                // 학습 패턴에 따른 우선순위 계산
                int priority = calculatePatternBasedPriority(analysis, questionType, overallPattern, consistencyScore);
                
                return ReviewQuestionSet.builder()
                    .questionType(questionType)
                    .wrongAnswerQuestionIds(filterWrongAnswersByType(wrongQuestionIds, questionType)) // 오답 문제 ID
                    .solvedQuestionIds(generateSolvedQuestionIds(questionType, solvedQuestions)) // 푼 문제 ID
                    .totalQuestions(totalQuestions) // 총 10문제
                    .wrongAnswerQuestions(wrongAnswerQuestions) // 오답 문제 수
                    .solvedQuestions(solvedQuestions) // 푼 문제 수
                    .priority(priority)
                    .estimatedTime(estimateReviewTime(totalQuestions, analysis.getAverageTimePerQuestion()))
                    .focusArea(analysis.getFocusAreas().contains(questionType))
                    .learningPattern(overallPattern)
                    .consistencyScore(consistencyScore)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 푼 문제 ID 생성 (더미 구현)
     */
    private List<String> generateSolvedQuestionIds(String questionType, int solvedQuestions) {
        // TODO: Problem Service API 호출로 해당 유형의 푼 문제 가져오기
        // 현재는 더미 데이터로 시뮬레이션
        List<String> solvedIds = new ArrayList<>();
        for (int i = 1; i <= solvedQuestions; i++) {
            solvedIds.add("solved_" + questionType + "_" + i);
        }
        return solvedIds;
    }

    /**
     * 특정 유형의 오답 문제만 필터링
     */
    private List<String> filterWrongAnswersByType(List<String> wrongQuestionIds, String questionType) {
        // TODO: Problem Service API 호출로 실제 문제 유형 확인
        // 현재는 더미 데이터로 시뮬레이션 (전체 오답 문제 반환)
        return wrongQuestionIds.stream()
            .filter(id -> isQuestionOfType(id, questionType))
            .collect(Collectors.toList());
    }

    /**
     * 오답 문제 수 계산 (총 10문제 내에서 할당)
     */
    private int calculateWrongAnswerQuestions(String questionType, 
                                           LearningPatternAnalysisService.CompleteLearningAnalysis analysis,
                                           String overallPattern) {
        
        // 기본 오답 문제 수 (총 10문제 내에서)
        int baseWrongQuestions = 3;
        
        // 학습 패턴에 따른 조정
        switch (overallPattern) {
            case "CONSISTENT_LEARNER":
                // 일관된 학습자: 적당한 오답 문제
                baseWrongQuestions = 2;
                break;
                
            case "VARIABLE_LEARNER":
                // 변동이 큰 학습자: 충분한 오답 문제
                baseWrongQuestions = 4;
                break;
                
            case "STRUGGLING_LEARNER":
                // 어려움을 겪는 학습자: 집중 오답 문제
                baseWrongQuestions = 5;
                break;
                
            case "FAST_LEARNER":
                // 빠른 학습자: 효율적 오답 문제
                baseWrongQuestions = 2;
                break;
        }
        
        // 취약 영역 여부에 따른 조정
        if (analysis.getFocusAreas().contains(questionType)) {
            baseWrongQuestions += 1; // 집중 학습 영역은 +1문제
        }
        
        // 실제 오답 문제 수에 따른 제한
        int availableWrongQuestions = (int) analysis.getWrongQuestionIds().stream()
            .filter(id -> isQuestionOfType(id, questionType))
            .count();
        
        // 최대 8문제까지만 오답 문제로 할당 (최소 2문제는 푼 문제)
        int maxWrongQuestions = Math.min(8, availableWrongQuestions);
        
        return Math.min(baseWrongQuestions, maxWrongQuestions);
    }

    /**
     * 학습 패턴 기반 우선순위 계산
     */
    private int calculatePatternBasedPriority(LearningPatternAnalysisService.CompleteLearningAnalysis analysis, 
                                           String questionType, String overallPattern, double consistencyScore) {
        
        // 기본 우선순위 (오답 문제 수 기반)
        int basePriority = calculateBasePriority(analysis.getWrongQuestionIds().size());
        
        // 학습 패턴에 따른 우선순위 조정
        int patternAdjustment = calculatePatternAdjustment(overallPattern, questionType);
        
        // 일관성 점수에 따른 우선순위 조정
        int consistencyAdjustment = calculateConsistencyAdjustment(consistencyScore);
        
        // 최종 우선순위 계산 (1: 높음, 2: 중간, 3: 낮음)
        int finalPriority = basePriority + patternAdjustment + consistencyAdjustment;
        
        return Math.max(1, Math.min(3, finalPriority));
    }

    /**
     * 기본 우선순위 계산
     */
    private int calculateBasePriority(int wrongAnswerCount) {
        if (wrongAnswerCount >= 5) return 1;      // 높은 우선순위
        else if (wrongAnswerCount >= 3) return 2; // 중간 우선순위
        else return 3;                            // 낮은 우선순위
    }

    /**
     * 학습 패턴에 따른 우선순위 조정
     */
    private int calculatePatternAdjustment(String overallPattern, String questionType) {
        switch (overallPattern) {
            case "CONSISTENT_LEARNER":
                // 일관된 학습자: 취약 영역에 집중
                return -1; // 우선순위 높임
                
            case "VARIABLE_LEARNER":
                // 변동이 큰 학습자: 안정성 향상에 집중
                return 0; // 기본 유지
                
            case "STRUGGLING_LEARNER":
                // 어려움을 겪는 학습자: 모든 영역에 집중
                return -1; // 우선순위 높임
                
            case "FAST_LEARNER":
                // 빠른 학습자: 세밀한 부분에 집중
                return 0; // 기본 유지
                
            default:
                return 0; // 기본 유지
        }
    }

    /**
     * 일관성 점수에 따른 우선순위 조정
     */
    private int calculateConsistencyAdjustment(double consistencyScore) {
        if (consistencyScore >= 0.8) return 0;      // 높은 일관성: 기본 유지
        else if (consistencyScore >= 0.6) return 0; // 보통 일관성: 기본 유지
        else return -1;                             // 낮은 일관성: 우선순위 높임
    }

    /**
     * 문제가 특정 유형에 속하는지 확인 (더미 구현)
     */
    private boolean isQuestionOfType(String questionId, String questionType) {
        // TODO: Problem Service API 호출로 실제 문제 유형 확인
        // 현재는 더미 데이터로 시뮬레이션
        return true;
    }

    /**
     * 자동으로 복습 세션 생성
     */
    private void automaticallyCreateReviewSessions(String userId, List<ReviewQuestionSet> reviewSets) {
        // 1. 높은 우선순위 세션 생성 (오답 세션)
        List<ReviewQuestionSet> highPrioritySets = reviewSets.stream()
            .filter(set -> set.getPriority() == 1)
            .collect(Collectors.toList());
        
        if (!highPrioritySets.isEmpty()) {
            createWrongAnswerSession(userId, highPrioritySets);
        }
        
        // 2. 중간 우선순위 세션 생성 (복습 세션)
        List<ReviewQuestionSet> mediumPrioritySets = reviewSets.stream()
            .filter(set -> set.getPriority() == 2)
            .collect(Collectors.toList());
        
        if (!mediumPrioritySets.isEmpty()) {
            createReviewSession(userId, mediumPrioritySets);
        }
        
        // 3. 전체 복습 세션 생성 (종합 복습)
        if (!reviewSets.isEmpty()) {
            createComprehensiveReviewSession(userId, reviewSets);
        }
    }

    /**
     * 오답 세션 생성
     */
    private void createWrongAnswerSession(String userId, List<ReviewQuestionSet> questionSets) {
        try {
            // 총 문제 수 계산 (기본 10문제 + 오답 문제)
            int totalQuestions = questionSets.stream()
                .mapToInt(set -> set.getTotalQuestions())
                .sum();
            
            if (totalQuestions > 0) {
                LearningSessionDto.SessionResponse wrongAnswerSession = 
                    learningServiceOrchestrator.startPersonalizedLearningSession(
                        userId, "WRONG_ANSWER", totalQuestions);
                
                log.info("오답 세션 자동 생성 완료: userId={}, sessionId={}, totalQuestions={}", 
                    userId, wrongAnswerSession.getSessionId(), totalQuestions);
            }
        } catch (Exception e) {
            log.error("오답 세션 자동 생성 실패: userId={}", userId, e);
        }
    }

    /**
     * 복습 세션 생성
     */
    private void createReviewSession(String userId, List<ReviewQuestionSet> questionSets) {
        try {
            // 총 문제 수 계산 (기본 10문제 + 오답 문제)
            int totalQuestions = questionSets.stream()
                .mapToInt(set -> set.getTotalQuestions())
                .sum();
            
            if (totalQuestions > 0) {
                LearningSessionDto.SessionResponse reviewSession = 
                    learningServiceOrchestrator.startPersonalizedLearningSession(
                        userId, "REVIEW", totalQuestions);
                
                log.info("복습 세션 자동 생성 완료: userId={}, sessionId={}, totalQuestions={}", 
                    userId, reviewSession.getSessionId(), totalQuestions);
            }
        } catch (Exception e) {
            log.error("복습 세션 자동 생성 실패: userId={}", userId, e);
        }
    }

    /**
     * 종합 복습 세션 생성
     */
    private void createComprehensiveReviewSession(String userId, List<ReviewQuestionSet> reviewSets) {
        try {
            // 총 문제 수 계산 (기본 10문제 + 오답 문제)
            int totalQuestions = reviewSets.stream()
                .mapToInt(set -> set.getTotalQuestions())
                .sum();
            
            if (totalQuestions > 0) {
                LearningSessionDto.SessionResponse comprehensiveSession = 
                    learningServiceOrchestrator.startPersonalizedLearningSession(
                        userId, "COMPREHENSIVE_REVIEW", totalQuestions);
                
                log.info("종합 복습 세션 자동 생성 완료: userId={}, sessionId={}, totalQuestions={}", 
                    userId, comprehensiveSession.getSessionId(), totalQuestions);
            }
        } catch (Exception e) {
            log.error("종합 복습 세션 자동 생성 실패: userId={}", userId, e);
        }
    }

    /**
     * 복습 시간 추정
     */
    private Integer estimateReviewTime(int questionCount, double averageTimePerQuestion) {
        // 기본 문제는 평균 시간, 오답 문제는 1.5배 + 추가 학습 시간
        return (int) (questionCount * averageTimePerQuestion * 1.2 + questionCount * 20);
    }

    /**
     * 복습 세션 완료 시 정답 처리된 문제 업데이트
     * 다음 복습에서 해당 문제가 오답 문제로 나오지 않도록 처리
     */
    public void updateReviewQuestionResults(String userId, String sessionId, 
                                         List<ReviewQuestionResult> questionResults) {
        try {
            log.info("복습 세션 완료 결과 업데이트: userId={}, sessionId={}, questionCount={}", 
                userId, sessionId, questionResults.size());
            
            // 정답 처리된 문제들을 데이터베이스에 저장
            saveCorrectlyAnsweredQuestions(userId, sessionId, questionResults);
            
            // 오답 문제 목록에서 정답 처리된 문제 제거
            removeCorrectlyAnsweredFromWrongAnswers(userId, questionResults);
            
            log.info("복습 세션 완료 결과 업데이트 완료: userId={}, sessionId={}", 
                userId, sessionId);
                
        } catch (Exception e) {
            log.error("복습 세션 완료 결과 업데이트 실패: userId={}, sessionId={}", 
                userId, sessionId, e);
        }
    }

    /**
     * 정답 처리된 문제들을 데이터베이스에 저장
     */
    private void saveCorrectlyAnsweredQuestions(String userId, String sessionId, 
                                             List<ReviewQuestionResult> questionResults) {
        // TODO: Problem Service API 호출로 정답 처리된 문제 정보 저장
        // 또는 Learning Service의 QuestionAnswer 엔티티에 저장
        
        List<ReviewQuestionResult> correctQuestions = questionResults.stream()
            .filter(result -> result.getIsCorrect())
            .collect(Collectors.toList());
        
        log.info("정답 처리된 문제 저장: userId={}, sessionId={}, correctCount={}", 
            userId, sessionId, correctQuestions.size());
    }

    /**
     * 오답 문제 목록에서 정답 처리된 문제 제거
     */
    private void removeCorrectlyAnsweredFromWrongAnswers(String userId, 
                                                       List<ReviewQuestionResult> questionResults) {
        // TODO: 사용자의 오답 문제 목록에서 정답 처리된 문제 ID 제거
        // 이렇게 하면 다음 복습에서 해당 문제가 오답 문제로 나오지 않음
        
        List<String> correctlyAnsweredIds = questionResults.stream()
            .filter(result -> result.getIsCorrect())
            .map(result -> result.getQuestionId())
            .collect(Collectors.toList());
        
        log.info("오답 문제 목록에서 정답 처리된 문제 제거: userId={}, removedCount={}", 
            userId, correctlyAnsweredIds.size());
    }

    // ===== DTO 클래스들 =====

    /**
     * 복습 문제 세트 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReviewQuestionSet {
        private String questionType;                    // 문제 유형
        private List<String> wrongAnswerQuestionIds;    // 오답 문제 ID 목록
        private List<String> solvedQuestionIds;         // 푼 문제 ID 목록
        private Integer totalQuestions;                 // 총 문제 수 (10문제)
        private Integer wrongAnswerQuestions;           // 오답 문제 수
        private Integer solvedQuestions;                 // 푼 문제 수
        private Integer priority;                       // 우선순위 (1: 높음, 2: 중간, 3: 낮음)
        private Integer estimatedTime;                  // 예상 소요 시간 (초)
        private boolean focusArea;                      // 집중 학습 영역 여부
        private String learningPattern;                 // 학습 패턴
        private double consistencyScore;                // 일관성 점수
    }

    /**
     * 복습 문제 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReviewQuestionResult {
        private String questionId;      // 문제 ID
        private String questionType;    // 문제 유형
        private Boolean isCorrect;      // 정답 여부
        private Integer timeSpent;      // 소요 시간 (초)
        private String userNotes;       // 사용자 메모
        private LocalDateTime answeredAt; // 답변 시간
    }
}
