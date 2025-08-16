package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 학습 완료 이벤트 DTO
 * 전체 학습 결과 분석 및 오답/복습 문제 제공을 위한 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningCompletedEvent {
    
    private String eventId;
    private String eventType = "LEARNING_COMPLETED";
    private LocalDateTime timestamp;
    
    // 세션 정보
    private String sessionId;
    private String userId;
    private String learningItemId; // 문제 유형
    
    // 학습 결과 요약
    private Integer totalQuestions; // 총 문제 갯수
    private Integer correctAnswers; // 정답 갯수
    private Integer wrongAnswers;  // 오답 갯수
    private Integer score; // 점수
    private Long totalDuration; // 총 소요 시간 (초)
    private Double averageTimePerQuestion; // 평균 소요 시간 (초)
    
    // 오답 분석 결과
    private List<String> wrongQuestionIds;
    private List<String> weakCategories; // 취약한 카테고리들
    private Map<String, Double> categoryAccuracy; // 카테고리별 정답률
    
    // 학습 패턴 분석 결과
    private String overallLearningPattern; // 전체 학습 패턴
    private Integer consecutiveCorrectMax; // 최대 연속 정답 수
    private Integer consecutiveWrongMax; // 최대 연속 오답 수
    private Double consistencyScore; // 일관성 점수 (0-100)
    
    // 복습 문제 추천 데이터
    private List<String> recommendedReviewQuestions; // 추천 복습 문제 ID들
    private List<String> recommendedWeakAreaQuestions; // 취약 영역 보강 문제 ID들
    private Map<String, Integer> questionPriority; // 문제별 우선순위 (높을수록 중요)
    
    // 개인화 학습 제안
    private String learningSuggestion; // 학습 제안 메시지
    private List<String> focusAreas; // 집중해야 할 영역들
    private Integer estimatedReviewTime; // 예상 복습 시간 (분)
}
