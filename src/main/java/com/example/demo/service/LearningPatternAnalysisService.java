package com.example.demo.service;

import com.example.demo.dto.analytics.LearningPatternAnalysisDTO;
import com.example.demo.dto.analytics.QuestionTypePerformance;
import com.example.demo.entity.LearningSession;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.repository.LearningSessionRepository;
import com.example.demo.entity.QuestionCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class LearningPatternAnalysisService {

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private LearningSessionRepository learningSessionRepository;
 

    /**
     * 개별 세션에 대한 학습 패턴 분석 수행
     * 외부 DTO를 직접 사용하여 분석 결과 반환
     */
    public LearningPatternAnalysisDTO performPatternAnalysis(LearningSessionResult sessionResult) {
        // 문제 유형별 성과 분석
        List<QuestionTypePerformance> questionTypePerformances = analyzeQuestionTypePerformance(
            sessionResult.getSessionId(), sessionResult.getUserId());
        
        // 외부 DTO로 직접 변환하여 반환
        return LearningPatternAnalysisDTO.builder()
                .analysisType("SESSION_ANALYSIS")
                .userId(sessionResult.getUserId())
                .sessionId(sessionResult.getSessionId())
                .questionTypePerformances(questionTypePerformances)
                .reviewRequiredTypes(extractReviewRequiredTypes(questionTypePerformances))
                .improvementRequiredTypes(extractImprovementRequiredTypes(questionTypePerformances))
                .strengthTypes(extractStrengthTypes(questionTypePerformances))
                .recentWrongQuestionIds(extractRecentWrongQuestionIds(sessionResult.getQuestionAnswers()))
                .longIntervalTypes(extractLongIntervalTypes(Arrays.asList()))
                .slowSolvingTypes(extractSlowSolvingTypes(questionTypePerformances))
                .overallAccuracyRate(calculateAccuracyRate(sessionResult))
                .averageSolvingTime(calculateAverageSolvingTime(sessionResult))
                .studyFrequency("WEEKLY")
                .preferredStudyTime("EVENING")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 전체 학습 기간에 대한 종합 분석 수행
     * 개별 세션 분석과 동일한 분석 로직을 사용하여 일관성 있는 결과 제공
     */
    public LearningPatternAnalysisDTO analyzeCompleteLearning(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return performCompleteLearningAnalysis(userId, startDate, endDate, null);
    }

    /**
     * 증분 분석을 지원하는 전체 학습 분석
     * 이전 분석 결과가 있으면 병합하여 성능 향상
     */
    public LearningPatternAnalysisDTO analyzeCompleteLearningIncremental(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 이전 분석 결과 조회 (최근 30일 내)
        LocalDateTime previousStartDate = startDate.minusDays(30);
        List<LearningPatternAnalysisDTO> previousAnalyses = findPreviousAnalyses(userId, previousStartDate, endDate);
        
        if (!previousAnalyses.isEmpty()) {
            // 가장 최근 분석 결과와 병합
            LearningPatternAnalysisDTO latestAnalysis = previousAnalyses.get(0);
            return performCompleteLearningAnalysisWithMerge(userId, startDate, endDate, latestAnalysis);
        } else {
            // 이전 분석이 없으면 전체 분석 수행
            return performCompleteLearningAnalysis(userId, startDate, endDate, null);
        }
    }

    /**
     * 전체 학습 분석 수행 (공통 로직)
     */
    private LearningPatternAnalysisDTO performCompleteLearningAnalysis(String userId, LocalDateTime startDate, LocalDateTime endDate, 
                                                                   LearningPatternAnalysisDTO previousAnalysis) {
        // 전체 기간의 모든 세션 데이터 수집
        List<LearningSession> sessions = learningSessionRepository.findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(
            userId, startDate, endDate);
        
        // 전체 기간의 모든 문제 답변 데이터 수집
        // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
        List<QuestionAnswer> allAnswers = new ArrayList<>();
        for (LearningSession session : sessions) {
            List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
            allAnswers.addAll(sessionAnswers);
        }
        
        return buildCompleteLearningAnalysis(userId, startDate, endDate, sessions, allAnswers, previousAnalysis);
    }

    /**
     * 이전 분석 결과와 병합하여 전체 학습 분석 수행
     */
    private LearningPatternAnalysisDTO performCompleteLearningAnalysisWithMerge(String userId, LocalDateTime startDate, LocalDateTime endDate,
                                                                           LearningPatternAnalysisDTO previousAnalysis) {
        // 이전 분석 이후의 새로운 데이터만 조회
        LocalDateTime lastAnalyzedAt = previousAnalysis.getAnalyzedAt();
        List<LearningSession> newSessions = learningSessionRepository.findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(
            userId, lastAnalyzedAt, endDate);
        
        // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
        List<QuestionAnswer> newAnswers = new ArrayList<>();
        for (LearningSession session : newSessions) {
            List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
            newAnswers.addAll(sessionAnswers);
        }
        
        // 이전 분석 결과와 새로운 데이터 병합
        return mergeWithPreviousAnalysis(userId, startDate, endDate, previousAnalysis, newSessions, newAnswers);
    }

    /**
     * 문제 유형별 성과 분석 - 개별 세션용
     */
    private List<QuestionTypePerformance> analyzeQuestionTypePerformance(String sessionId, String userId) {
        return analyzeQuestionTypePerformanceCommon(sessionId, userId, null, null);
    }

    /**
     * 문제 유형별 성과 분석 - 전체 학습용
     */
    private List<QuestionTypePerformance> analyzeQuestionTypePerformanceByUserId(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return analyzeQuestionTypePerformanceCommon(null, userId, startDate, endDate);
    }

    /**
     * 문제 유형별 성과 분석 - 공통 로직 (중복 제거)
     */
    private List<QuestionTypePerformance> analyzeQuestionTypePerformanceCommon(String sessionId, String userId, 
                                                                             LocalDateTime startDate, LocalDateTime endDate) {
        List<QuestionTypePerformance> performances = new ArrayList<>();
        
        for (QuestionCategory.QuestionType type : QuestionCategory.QuestionType.values()) {
            List<QuestionAnswer> typeAnswers;
            
            if (sessionId != null) {
                // 세션별 분석 (questionType은 String으로 저장됨)
                typeAnswers = questionAnswerRepository.findBySessionIdAndQuestionType(sessionId, type.name());
            } else {
                // 사용자별 기간별 분석
                // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
                typeAnswers = new ArrayList<>();
                List<LearningSession> userSessions = learningSessionRepository.findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(
                    userId, startDate, endDate);
                for (LearningSession session : userSessions) {
                    List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdAndQuestionType(
                        session.getSessionId(), type.name());
                    typeAnswers.addAll(sessionAnswers);
                }
            }
            
            if (!typeAnswers.isEmpty()) {
                QuestionTypePerformance performance = buildQuestionTypePerformance(typeAnswers, type.name());
                performances.add(performance);
            }
        }
        
        return performances;
    }

    /**
     * 문제 유형별 성과 정보 생성 - 공통 로직
     */
    private QuestionTypePerformance buildQuestionTypePerformance(List<QuestionAnswer> typeAnswers, String questionType) {
        int totalQuestions = typeAnswers.size();
        int correctAnswers = (int) typeAnswers.stream().filter(qa -> qa.getIsCorrect()).count();
        double accuracyRate = (double) correctAnswers / totalQuestions * 100;
        double averageTime = typeAnswers.stream()
            .mapToLong(qa -> qa.getTimeSpent() != null ? qa.getTimeSpent() : 0)
            .average()
            .orElse(0.0);
        
        return QuestionTypePerformance.builder()
                .questionType(questionType)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .accuracyRate(accuracyRate)
                .averageTime(averageTime)
                .build();
    }

    /**
     * 전체 학습 분석 결과 생성 - 공통 로직
     */
    private LearningPatternAnalysisDTO buildCompleteLearningAnalysis(String userId, LocalDateTime startDate, LocalDateTime endDate,
                                                                 List<LearningSession> sessions, List<QuestionAnswer> allAnswers,
                                                                 LearningPatternAnalysisDTO previousAnalysis) {
        // 전체 기간 통합 결과 생성
        LearningSessionResult completeResult = LearningSessionResult.builder()
                .userId(userId)
                .sessionId(null) // 전체 분석이므로 세션 ID 없음
                .totalQuestions(allAnswers.size())
                .correctAnswers((int) allAnswers.stream().filter(qa -> qa.getIsCorrect()).count())
                .totalDuration(calculateTotalDuration(sessions))
                .questionAnswers(allAnswers)
                .build();
        
        // 문제 유형별 성과 분석
        List<QuestionTypePerformance> questionTypePerformances = analyzeQuestionTypePerformanceByUserId(userId, startDate, endDate);
        
        // 외부 DTO로 직접 변환하여 반환
        return LearningPatternAnalysisDTO.builder()
                .analysisType("COMPLETE_ANALYSIS")
                .userId(userId)
                .sessionId(null) // 전체 분석이므로 세션 ID 없음
                .questionTypePerformances(questionTypePerformances)
                .reviewRequiredTypes(extractReviewRequiredTypes(questionTypePerformances))
                .improvementRequiredTypes(extractImprovementRequiredTypes(questionTypePerformances))
                .strengthTypes(extractStrengthTypes(questionTypePerformances))
                .recentWrongQuestionIds(extractRecentWrongQuestionIds(allAnswers))
                .longIntervalTypes(extractLongIntervalTypes(sessions))
                .slowSolvingTypes(extractSlowSolvingTypes(questionTypePerformances))
                .overallAccuracyRate(calculateAccuracyRate(completeResult))
                .averageSolvingTime(calculateAverageSolvingTime(completeResult))
                .studyFrequency(analyzeLearningFrequency(sessions))
                .preferredStudyTime(analyzeTimePattern(sessions))
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 이전 분석 결과와 새로운 데이터 병합
     */
    private LearningPatternAnalysisDTO mergeWithPreviousAnalysis(String userId, LocalDateTime startDate, LocalDateTime endDate,
                                                            LearningPatternAnalysisDTO previousAnalysis,
                                                            List<LearningSession> newSessions, List<QuestionAnswer> newAnswers) {
        // 새로운 데이터만으로 분석 결과 생성
        LearningSessionResult newResult = LearningSessionResult.builder()
                .userId(userId)
                .sessionId(null)
                .totalQuestions(newAnswers.size())
                .correctAnswers((int) newAnswers.stream().filter(qa -> qa.getIsCorrect()).count())
                .totalDuration(calculateTotalDuration(newSessions))
                .questionAnswers(newAnswers)
                .build();
        
        // 새로운 데이터 분석
        List<QuestionTypePerformance> newQuestionTypePerformances = analyzeQuestionTypePerformanceCommon(null, userId, startDate, endDate);
        
        // 이전 분석 결과와 새로운 데이터 병합
        List<QuestionTypePerformance> mergedQuestionTypePerformances = mergeQuestionTypePerformances(
            previousAnalysis.getQuestionTypePerformances(), newQuestionTypePerformances);
        
        return LearningPatternAnalysisDTO.builder()
                .analysisType("COMPLETE_ANALYSIS")
                .userId(userId)
                .sessionId(null)
                .questionTypePerformances(mergedQuestionTypePerformances)
                .reviewRequiredTypes(extractReviewRequiredTypes(mergedQuestionTypePerformances))
                .improvementRequiredTypes(extractImprovementRequiredTypes(mergedQuestionTypePerformances))
                .strengthTypes(extractStrengthTypes(mergedQuestionTypePerformances))
                .recentWrongQuestionIds(extractRecentWrongQuestionIds(newAnswers))
                .longIntervalTypes(extractLongIntervalTypes(newSessions))
                .slowSolvingTypes(extractSlowSolvingTypes(mergedQuestionTypePerformances))
                .overallAccuracyRate(calculateAccuracyRate(newResult))
                .averageSolvingTime(calculateAverageSolvingTime(newResult))
                .studyFrequency(analyzeLearningFrequency(newSessions))
                .preferredStudyTime(analyzeTimePattern(newSessions))
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 문제 유형별 성과 병합
     */
    private List<QuestionTypePerformance> mergeQuestionTypePerformances(
            List<QuestionTypePerformance> previous, List<QuestionTypePerformance> current) {
        
        Map<String, QuestionTypePerformance> mergedMap = new HashMap<>();
        
        // 이전 데이터 추가
        for (QuestionTypePerformance prev : previous) {
            mergedMap.put(prev.getQuestionType(), prev);
        }
        
        // 현재 데이터와 병합
        for (QuestionTypePerformance curr : current) {
            QuestionTypePerformance existing = mergedMap.get(curr.getQuestionType());
            if (existing != null) {
                // 기존 데이터와 병합
                QuestionTypePerformance merged = mergeQuestionTypePerformance(existing, curr);
                mergedMap.put(curr.getQuestionType(), merged);
            } else {
                // 새로운 문제 유형
                mergedMap.put(curr.getQuestionType(), curr);
            }
        }
        
        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 개별 문제 유형 성과 병합
     */
    private QuestionTypePerformance mergeQuestionTypePerformance(QuestionTypePerformance previous, QuestionTypePerformance current) {
        int totalQuestions = previous.getTotalQuestions() + current.getTotalQuestions();
        int totalCorrectAnswers = previous.getCorrectAnswers() + current.getCorrectAnswers();
        double accuracyRate = totalQuestions > 0 ? (double) totalCorrectAnswers / totalQuestions * 100 : 0.0;
        
        // 평균 시간은 가중 평균으로 계산
        double avgTime = (previous.getTotalQuestions() * previous.getAverageTime() + 
                         current.getTotalQuestions() * current.getAverageTime()) / totalQuestions;
        
        return QuestionTypePerformance.builder()
                .questionType(previous.getQuestionType())
                .totalQuestions(totalQuestions)
                .correctAnswers(totalCorrectAnswers)
                .accuracyRate(accuracyRate)
                .averageTime(avgTime)
                .build();
    }

    /**
     * 이전 분석 결과 조회
     */
    private List<LearningPatternAnalysisDTO> findPreviousAnalyses(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 실제 구현에서는 LearningPatternAnalysisRepository를 통해 조회
        // 현재는 빈 리스트 반환 (구현 필요)
        return new ArrayList<>();
    }

    /**
     * 세션들의 총 학습 시간을 계산 (초 단위)
     * startedAt과 completedAt을 이용하여 실제 학습 시간 계산
     */
    private long calculateTotalDuration(List<LearningSession> sessions) {
        return sessions.stream()
                .mapToLong(s -> calculateSessionDuration(s))
                .sum();
    }

    /**
     * 개별 세션의 학습 시간을 계산 (초 단위)
     * completedAt이 null이면 0 반환 (완료되지 않은 세션)
     */
    private long calculateSessionDuration(LearningSession session) {
        if (session.getStartedAt() == null || session.getCompletedAt() == null) {
            return 0L;
        }
        
        // Duration을 이용하여 초 단위로 계산
        return java.time.Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();
    }

    // === 외부 DTO 생성을 위한 헬퍼 메서드들 ===

    private List<String> extractReviewRequiredTypes(List<QuestionTypePerformance> performances) {
        return performances.stream()
                .filter(p -> p.getAccuracyRate() < 60.0)
                .map(QuestionTypePerformance::getQuestionType)
                .collect(Collectors.toList());
    }

    private List<String> extractImprovementRequiredTypes(List<QuestionTypePerformance> performances) {
        return performances.stream()
                .filter(p -> p.getAccuracyRate() >= 60.0 && p.getAccuracyRate() < 80.0)
                .map(QuestionTypePerformance::getQuestionType)
                .collect(Collectors.toList());
    }

    private List<String> extractStrengthTypes(List<QuestionTypePerformance> performances) {
        return performances.stream()
                .filter(p -> p.getAccuracyRate() >= 80.0)
                .map(QuestionTypePerformance::getQuestionType)
                .collect(Collectors.toList());
    }

    private List<String> extractRecentWrongQuestionIds(List<QuestionAnswer> answers) {
        // 최근 2주 내 오답한 문제 ID들 반환
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
        return answers.stream()
                .filter(qa -> !qa.getIsCorrect() && qa.getAnsweredAt().isAfter(twoWeeksAgo))
                .map(QuestionAnswer::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractLongIntervalTypes(List<LearningSession> sessions) {
        // 오래 전에 학습한 문제 유형들 반환 (간단한 구현)
        return Arrays.asList("VOCABULARY"); // 임시로 고정값 반환
    }

    private List<String> extractSlowSolvingTypes(List<QuestionTypePerformance> performances) {
        // 풀이 시간이 긴 문제 유형들 반환 (간단한 구현)
        return performances.stream()
                .filter(p -> p.getAverageTime() > 60.0) // 60초 이상
                .map(QuestionTypePerformance::getQuestionType)
                .collect(Collectors.toList());
    }

    // === 헬퍼 메서드들 ===

    private double calculateAccuracyRate(LearningSessionResult result) {
        return result.getTotalQuestions() > 0 ? 
            (double) result.getCorrectAnswers() / result.getTotalQuestions() * 100 : 0.0;
    }

    private double calculateAverageSolvingTime(LearningSessionResult result) {
        return result.getTotalQuestions() > 0 ? 
            (double) result.getTotalDuration() / result.getTotalQuestions() : 0.0;
    }

    private String analyzeLearningFrequency(List<LearningSession> sessions) {
        if (sessions.size() < 2) return "INSUFFICIENT_DATA";
        
        // 세션 간격 분석
        return "REGULAR"; // 간단한 구현
    }

    private String analyzeTimePattern(List<LearningSession> sessions) {
        if (sessions.isEmpty()) return "UNKNOWN";
        
        // 시간대별 패턴 분석
        return "MIXED"; // 간단한 구현
    }

    // === 내부 DTO 클래스 - 외부 DTO 생성을 위해 필요 ===

    /**
     * 학습 세션 결과 데이터를 담는 DTO
     * 개별 세션과 전체 학습 분석에서 공통으로 사용하는 입력 데이터
     */
    public static class LearningSessionResult {
        private String userId;
        private String sessionId; // 전체 분석의 경우 null
        private int totalQuestions;
        private int correctAnswers;
        private long totalDuration;
        private List<QuestionAnswer> questionAnswers;

        public static LearningSessionResultBuilder builder() {
            return new LearningSessionResultBuilder();
        }

        public static class LearningSessionResultBuilder {
            private LearningSessionResult result = new LearningSessionResult();

            public LearningSessionResultBuilder userId(String userId) {
                result.userId = userId;
                return this;
            }

            public LearningSessionResultBuilder sessionId(String sessionId) {
                result.sessionId = sessionId;
                return this;
            }

            public LearningSessionResultBuilder totalQuestions(int totalQuestions) {
                result.totalQuestions = totalQuestions;
                return this;
            }

            public LearningSessionResultBuilder correctAnswers(int correctAnswers) {
                result.correctAnswers = correctAnswers;
                return this;
            }

            public LearningSessionResultBuilder totalDuration(long totalDuration) {
                result.totalDuration = totalDuration;
                return this;
            }

            public LearningSessionResultBuilder questionAnswers(List<QuestionAnswer> questionAnswers) {
                result.questionAnswers = questionAnswers;
                return this;
            }

            public LearningSessionResult build() {
                return result;
            }
        }

        // Getter 메서드들
        public String getUserId() { return userId; }
        public String getSessionId() { return sessionId; }
        public int getTotalQuestions() { return totalQuestions; }
        public int getCorrectAnswers() { return correctAnswers; }
        public long getTotalDuration() { return totalDuration; }
        public List<QuestionAnswer> getQuestionAnswers() { return questionAnswers; }
    }
}
