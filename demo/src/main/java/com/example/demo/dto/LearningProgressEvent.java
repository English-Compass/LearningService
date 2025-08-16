package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학습 진행 상황 이벤트 DTO
 * 오답/복습 문제 제공을 위한 데이터 수집
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressEvent {
    
    private String eventId;
    private String eventType = "LEARNING_PROGRESS";
    private LocalDateTime timestamp;
    
    // 세션 정보
    private String sessionId;
    private String userId;
    private String learningItemId;
    
    // 문제 답변 정보
    private String questionId;
    private Boolean isCorrect;
    private Integer timeSpent; // 문제 풀이 시간 (초)
    private String userNotes;
    
    // 학습 진행 상황
    private Integer currentQuestionNumber;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Double progressPercentage;
    
    // 문제 분류 정보 (취약 영역 분석용)
    private String majorCategory;
    private String minorCategory;
    private String questionType;
    private Integer difficulty;
    
    // 학습 패턴 분석 데이터
    private Double averageTimePerQuestion; // 문제당 평균 시간
    private String learningPattern; // 학습 패턴 (FAST_LEARNER, MODERATE_LEARNER, CAREFUL_LEARNER, SLOW_LEARNER)
}
