package com.example.demo.dto;

import com.example.demo.entity.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * QuestionAnswer 엔티티 기반 DTO 클래스들
 */
public class QuestionAnswerDto {

    /**
     * 문제 답변 제출 요청 DTO
     * QuestionAnswer 엔티티의 입력 필드들
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitAnswerRequest {
        // QuestionAnswer.questionId
        private String questionId;
        
        // QuestionAnswer.userAnswer (A, B, C)
        private String userAnswer;
        
        // QuestionAnswer.timeSpent
        private Integer timeSpent;
        
        // QuestionAnswer.userNotes
        private String userNotes;
    }

    /**
     * 문제 답변 응답 DTO
     * QuestionAnswer 엔티티의 결과 필드들
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerResponse {
        // QuestionAnswer 기본 정보
        private String questionId;
        private String questionText;
        
        // QuestionAnswer 보기 정보
        private String optionA;
        private String optionB;
        private String optionC;
        
        // QuestionAnswer 답변 결과
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        
        // QuestionAnswer 점수 정보
        private Integer pointsPerQuestion;
        private Integer earnedPoints;
        
        // QuestionAnswer 시간 정보
        private Integer timeSpent;
        private LocalDateTime answeredAt;
        
        // QuestionAnswer 해설 및 메모
        private String explanation;
        private String userNotes;
        
        // QuestionAnswer 분류 정보
        private String majorCategory;
        private String minorCategory;
        private String questionType;
        private Integer difficulty;
        private String tags;
        
        // 계산된 값들
        private String userAnswerText; // getOptionByAnswer(userAnswer)
        private String correctAnswerText; // getOptionByAnswer(correctAnswer)
    }

    /**
     * 문제 답변 요약 DTO
     * 목록 조회용
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerSummary {
        private String questionId;
        private String questionText;
        private String userAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private Integer earnedPoints;
        private Integer timeSpent;
        private LocalDateTime answeredAt;
        private String majorCategory;
        private String minorCategory;
        private String questionType;
        private Integer difficulty;
    }

    /**
     * 문제 답변 통계 DTO
     * 카테고리별 성과 분석용
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerStatistics {
        private String sessionId;
        private String userId;
        
        // 전체 통계
        private Integer totalQuestions;
        private Integer correctAnswers;
        private Integer wrongAnswers;
        private Double accuracyRate;
        
        // 카테고리별 통계
        private CategoryPerformance majorCategoryPerformance;
        private CategoryPerformance minorCategoryPerformance;
        private CategoryPerformance questionTypePerformance;
        
        // 시간 통계
        private Double averageTimePerQuestion;
        private Integer totalTimeSpent;
        
        // 점수 통계
        private Integer totalEarnedPoints;
        private Integer maxPossiblePoints;
        private Double scorePercentage;
    }

    /**
     * 카테고리별 성과 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private String categoryName;
        private Integer totalQuestions;
        private Integer correctAnswers;
        private Integer wrongAnswers;
        private Double accuracyRate;
        private Double averageTime;
        private Integer totalEarnedPoints;
    }

    /**
     * 문제 풀이 진행 상황 DTO
     * 실시간 진행 상황 표시용
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInfo {
        private String sessionId;
        private Integer currentQuestionNumber;
        private Integer totalQuestions;
        private Double progressPercentage;
        private String currentQuestionId;
        private String nextQuestionId;
        private Boolean hasPreviousQuestion;
        private Boolean hasNextQuestion;
        private Integer answeredQuestions;
        private Integer remainingQuestions;
    }
}
