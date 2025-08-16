package com.example.demo.service;

import com.example.demo.dto.LearningCompletedEvent;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.repository.QuestionAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 학습 패턴 분석 서비스
 * 학습 완료 이벤트를 소비하여 학습자의 답변 패턴을 분석하고, 
 * 분석 결과를 ReviewService에 전달하여 개인화된 학습 제안 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPatternAnalysisService {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final ReviewService reviewService;

    /**
     * 학습 완료 이벤트 리스너
     * 전체 학습 완료 시 최종 분석 및 개인화된 복습 문제 생성
     */
    @EventListener
    public void handleLearningCompletedEvent(LearningCompletedEvent event) {
        try {
            log.info("학습 완료 이벤트 수신: sessionId={}, userId={}", 
                event.getSessionId(), event.getUserId());
            
            // 1. 전체 학습 패턴 분석
            CompleteLearningAnalysis analysis = analyzeCompleteLearning(event);
            
            // 2. 분석 결과를 ReviewService에 전달하여 개인화된 복습 문제 세트 생성
            reviewService.createPersonalizedReviewSet(analysis);
            
            log.info("학습 완료 패턴 분석 완료: sessionId={}, pattern={}, score={}", 
                event.getSessionId(), analysis.getOverallLearningPattern(), event.getScore());
                
        } catch (Exception e) {
            log.error("학습 완료 이벤트 처리 실패: sessionId={}", event.getSessionId(), e);
        }
    }

    /**
     * 학습 패턴 분석
     */
    private LearningPatternInfo analyzeLearningPattern(String sessionId) {
        List<QuestionAnswer> answers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(sessionId);
        
        if (answers.isEmpty()) {
            return LearningPatternInfo.builder()
                .averageTimePerQuestion(0.0)
                .learningPattern("NEW_LEARNER")
                .build();
        }
        
        // 평균 시간 계산
        double averageTime = calculateAverageTime(answers);
        
        // 학습 패턴 결정 (시간만 고려)
        String pattern = determineLearningPattern(averageTime);
        
        return LearningPatternInfo.builder()
            .averageTimePerQuestion(averageTime)
            .learningPattern(pattern)
            .detailedPattern(pattern)
            .build();
    }

    /**
     * 전체 학습 완료 분석
     */
    public CompleteLearningAnalysis analyzeCompleteLearning(LearningCompletedEvent event) {
        List<QuestionAnswer> answers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(event.getSessionId());
        
        // 기본 통계
        long totalDuration = calculateTotalDuration(answers);
        double averageTime = calculateAverageTime(answers);
        List<String> wrongQuestions = getWrongQuestionIds(answers);
        
        // 문제 유형별 성과 분석 (3가지 고정 유형)
        List<QuestionTypePerformance> questionTypePerformances = analyzeQuestionTypePerformance(answers);
        
        // 취약 영역 식별 (3가지 문제 유형 기준)
        List<String> weakQuestionTypes = identifyWeakQuestionTypes(questionTypePerformances);
        
        // 학습 패턴 분석
        String overallPattern = determineOverallLearningPattern(answers);
        double consistencyScore = calculateConsistencyScore(answers);
        
        // 복습 문제 추천 (오답 문제들만)
        List<String> reviewQuestions = recommendReviewQuestions(answers);
        
        // 학습 제안
        String suggestion = generateLearningSuggestion(overallPattern, consistencyScore, weakQuestionTypes);
        List<String> focusAreas = determineFocusAreas(weakQuestionTypes, questionTypePerformances);
        int estimatedReviewTime = estimateReviewTime(wrongQuestions.size(), averageTime);
        
        return CompleteLearningAnalysis.builder()
            .sessionId(event.getSessionId())
            .userId(event.getUserId())
            .totalDuration(totalDuration)
            .averageTimePerQuestion(averageTime)
            .wrongQuestionIds(wrongQuestions)
            .weakQuestionTypes(weakQuestionTypes) // 3가지 문제 유형 기준
            .questionTypePerformances(questionTypePerformances) // 문제 유형별 상세 성과
            .overallLearningPattern(overallPattern)
            .consistencyScore(consistencyScore)
            .recommendedReviewQuestions(reviewQuestions)
            .learningSuggestion(suggestion)
            .focusAreas(focusAreas)
            .estimatedReviewTime(estimatedReviewTime)
            .build();
    }

    // ===== 분석 유틸리티 메서드들 =====

    // 연속 정답 계산 메서드는 제거됨 (시간 중심 분석으로 변경)

    // 연속 오답 계산 메서드는 제거됨 (시간 중심 분석으로 변경)

    private double calculateAverageTime(List<QuestionAnswer> answers) {
        return answers.stream()
            .filter(answer -> answer.getTimeSpent() != null)
            .mapToInt(QuestionAnswer::getTimeSpent)
            .average()
            .orElse(0.0);
    }

    private long calculateTotalDuration(List<QuestionAnswer> answers) {
        if (answers.size() < 2) return 0;
        
        QuestionAnswer first = answers.get(0);
        QuestionAnswer last = answers.get(answers.size() - 1);
        
        if (first.getAnsweredAt() != null && last.getAnsweredAt() != null) {
            return Duration.between(first.getAnsweredAt(), last.getAnsweredAt()).getSeconds();
        }
        return 0;
    }

    private List<String> getWrongQuestionIds(List<QuestionAnswer> answers) {
        return answers.stream()
            .filter(answer -> !answer.getIsCorrect())
            .map(QuestionAnswer::getQuestionId)
            .collect(Collectors.toList());
    }

    /**
     * 카테고리별 성과 분석
     */
    private Map<String, Double> calculateCategoryAccuracy(List<QuestionAnswer> answers) {
        return answers.stream()
            .collect(Collectors.groupingBy(
                QuestionAnswer::getQuestionType, // 문제 유형별로 그룹화
                Collectors.averagingInt(answer -> answer.getIsCorrect() ? 1 : 0)
            ));
    }

    /**
     * 취약 영역 식별 (3가지 문제 유형 기준)
     */
    private List<String> identifyWeakCategories(Map<String, Double> categoryAccuracy) {
        return categoryAccuracy.entrySet().stream()
            .filter(entry -> entry.getValue() < 0.7) // 70% 미만을 취약 영역으로 간주
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 문제 유형별 성과 분석
     */
    private List<QuestionTypePerformance> analyzeQuestionTypePerformance(List<QuestionAnswer> answers) {
        List<QuestionTypePerformance> performances = new ArrayList<>();
        
        // 3가지 문제 유형별 성과 분석
        String[] questionTypes = {"FILL_IN_THE_BLANK", "SYNONYM_SELECTION", "PRONUNCIATION_RECOGNITION"};
        
        for (String type : questionTypes) {
            List<QuestionAnswer> typeAnswers = answers.stream()
                .filter(answer -> type.equals(answer.getQuestionType()))
                .collect(Collectors.toList());
            
            if (!typeAnswers.isEmpty()) {
                double accuracy = typeAnswers.stream()
                    .mapToInt(answer -> answer.getIsCorrect() ? 1 : 0)
                    .average()
                    .orElse(0.0);
                
                int totalCount = typeAnswers.size();
                int correctCount = (int) typeAnswers.stream()
                    .filter(QuestionAnswer::getIsCorrect)
                    .count();
                
                double averageTime = typeAnswers.stream()
                    .filter(answer -> answer.getTimeSpent() != null)
                    .mapToInt(QuestionAnswer::getTimeSpent)
                    .average()
                    .orElse(0.0);
                
                int difficulty = typeAnswers.stream()
                    .filter(answer -> answer.getDifficulty() != null)
                    .mapToInt(QuestionAnswer::getDifficulty)
                    .max()
                    .orElse(1);
                
                QuestionTypePerformance performance = QuestionTypePerformance.builder()
                    .questionType(type)
                    .totalQuestions(totalCount)
                    .correctAnswers(correctCount)
                    .accuracy(accuracy)
                    .isWeakArea(accuracy < 0.7)
                    .averageTime(averageTime)
                    .difficulty(difficulty)
                    .build();
                
                performances.add(performance);
            }
        }
        
        return performances;
    }

    /**
     * 취약한 문제 유형 식별 (3가지 고정 유형 기준)
     */
    private List<String> identifyWeakQuestionTypes(List<QuestionTypePerformance> questionTypePerformances) {
        return questionTypePerformances.stream()
            .filter(QuestionTypePerformance::isWeakArea)
            .map(QuestionTypePerformance::getQuestionType)
            .collect(Collectors.toList());
    }

    private String determineLearningPattern(double averageTime) {
        // 시간만 고려한 학습 패턴 분류
        if (averageTime < 20) {
            return "FAST_LEARNER"; // 빠른 학습자 (평균 시간 < 20초)
        } else if (averageTime < 40) {
            return "MODERATE_LEARNER"; // 보통 속도 학습자 (평균 시간 20-40초)
        } else if (averageTime < 60) {
            return "CAREFUL_LEARNER"; // 신중한 학습자 (평균 시간 40-60초)
        } else {
            return "SLOW_LEARNER"; // 느린 학습자 (평균 시간 60초+)
        }
    }

    private String determineOverallLearningPattern(List<QuestionAnswer> answers) {
        if (answers.isEmpty()) return "NO_DATA";
        
        long correctCount = answers.stream().filter(QuestionAnswer::getIsCorrect).count();
        double accuracy = (double) correctCount / answers.size();
        double avgTime = calculateAverageTime(answers);
        
        // 시간과 정답 개수만 고려한 패턴 분류
        if (accuracy >= 0.8 && avgTime < 30) {
            return "EXCELLENT"; // 우수한 학습자 (정답률 80%+, 빠른 속도)
        } else if (accuracy >= 0.8 && avgTime >= 30) {
            return "ACCURATE_BUT_SLOW"; // 정확하지만 느린 학습자 (정답률 80%+, 느린 속도)
        } else if (accuracy >= 0.6 && avgTime < 40) {
            return "GOOD"; // 좋은 학습자 (정답률 60%+, 적당한 속도)
        } else if (accuracy >= 0.6 && avgTime >= 40) {
            return "GOOD_BUT_SLOW"; // 좋지만 느린 학습자 (정답률 60%+, 느린 속도)
        } else if (accuracy >= 0.4) {
            return "AVERAGE"; // 평균적인 학습자 (정답률 40%+)
        } else {
            return "NEEDS_IMPROVEMENT"; // 개선이 필요한 학습자 (정답률 < 40%)
        }
    }

    // 최대 연속 정답/오답 계산 메서드들은 제거됨 (시간 중심 분석으로 변경)

    private double calculateConsistencyScore(List<QuestionAnswer> answers) {
        if (answers.size() < 2) return 100.0;
        
        int consistencyBreaks = 0;
        for (int i = 1; i < answers.size(); i++) {
            if (answers.get(i).getIsCorrect() != answers.get(i-1).getIsCorrect()) {
                consistencyBreaks++;
            }
        }
        
        double consistencyRatio = 1.0 - ((double) consistencyBreaks / (answers.size() - 1));
        return consistencyRatio * 100;
    }

    private List<String> recommendReviewQuestions(List<QuestionAnswer> answers) {
        return answers.stream()
            .filter(answer -> !answer.getIsCorrect())
            .map(QuestionAnswer::getQuestionId)
            .distinct()
            .limit(5) // 최대 5개 문제 추천
            .collect(Collectors.toList());
    }

    private List<String> recommendWeakAreaQuestions(List<QuestionAnswer> answers, List<String> weakCategories) {
        return answers.stream()
            .filter(answer -> weakCategories.contains(answer.getMajorCategory()))
            .map(QuestionAnswer::getQuestionId)
            .distinct()
            .limit(3) // 취약 영역당 최대 3개 문제
            .collect(Collectors.toList());
    }

    private Map<String, Integer> calculateQuestionPriority(List<QuestionAnswer> answers) {
        Map<String, Integer> priority = new HashMap<>();
        
        for (QuestionAnswer answer : answers) {
            if (!answer.getIsCorrect()) {
                priority.put(answer.getQuestionId(), 3); // 오답 문제는 높은 우선순위
            } else if (answer.getTimeSpent() != null && answer.getTimeSpent() > 60) {
                priority.put(answer.getQuestionId(), 2); // 오래 걸린 문제는 중간 우선순위
            } else {
                priority.put(answer.getQuestionId(), 1); // 정답 문제는 낮은 우선순위
            }
        }
        
        return priority;
    }

    private String generateLearningSuggestion(String pattern, double consistency, List<String> weakCategories) {
        if (pattern.equals("EXCELLENT")) {
            return "훌륭한 학습 성과입니다! 다음 단계로 넘어가보세요.";
        } else if (pattern.equals("NEEDS_IMPROVEMENT")) {
            return "기본 개념을 다시 한번 정리해보세요. 꾸준한 연습이 도움이 될 것입니다.";
        } else if (consistency < 50) {
            return "학습 패턴이 일정하지 않습니다. 집중력을 높여보세요.";
        } else if (!weakCategories.isEmpty()) {
            return String.format("%s 영역을 집중적으로 학습해보세요.", String.join(", ", weakCategories));
        } else {
            return "꾸준한 학습을 통해 실력을 향상시켜보세요.";
        }
    }

    /**
     * 집중 학습 영역 결정 (3가지 문제 유형 기준)
     */
    private List<String> determineFocusAreas(List<String> weakQuestionTypes, List<QuestionTypePerformance> questionTypePerformances) {
        List<String> focusAreas = new ArrayList<>(weakQuestionTypes);
        
        // 정답률이 70-80%인 문제 유형도 추가 (약간 부족한 영역)
        for (QuestionTypePerformance performance : questionTypePerformances) {
            if (performance.getAccuracy() >= 0.7 && performance.getAccuracy() < 0.8) {
                focusAreas.add(performance.getQuestionType());
            }
        }
        
        return focusAreas.stream().distinct().collect(Collectors.toList());
    }

    private int estimateReviewTime(int wrongQuestionsCount, double averageTime) {
        // 오답 문제당 평균 시간의 1.5배 + 추가 학습 시간
        int baseTime = (int) (wrongQuestionsCount * averageTime * 1.5);
        int additionalTime = wrongQuestionsCount * 2; // 문제당 2분 추가
        return Math.max(10, baseTime + additionalTime); // 최소 10분
    }

    // ===== 내부 DTO 클래스들 =====

    /**
     * 문제 유형별 성과 데이터 (타입 안전한 구조)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypePerformance {
        private String questionType;
        private int totalQuestions;
        private int correctAnswers;
        private double accuracy;
        private boolean isWeakArea;
        private double averageTime;
        private int difficulty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningProgressAnalysis {
        private String sessionId;
        private String userId;
        private String learningItemId;
        private String questionId;
        private Boolean isCorrect;
        private Integer timeSpent;
        private String userNotes;
        private Integer currentQuestionNumber;
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private Integer correctAnswers;
        private Integer wrongAnswers;
        private Double progressPercentage;
        private Double averageTimePerQuestion;
        private String learningPattern;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPatternInfo {
        private Double averageTimePerQuestion;
        private String learningPattern;
        private String detailedPattern; // FAST_LEARNER, MODERATE_LEARNER 등
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteLearningAnalysis {
        private String sessionId;
        private String userId;
        private Long totalDuration;
        private Double averageTimePerQuestion;
        private List<String> wrongQuestionIds;
        private List<String> weakQuestionTypes; // 일관된 명명
        private List<QuestionTypePerformance> questionTypePerformances; // 타입 안전한 구조
        private String overallLearningPattern;
        private Double consistencyScore;
        private List<String> recommendedReviewQuestions;
        private String learningSuggestion;
        private List<String> focusAreas;
        private Integer estimatedReviewTime;
    }
}
