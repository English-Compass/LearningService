package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 학습 성과 카드 DTO (대시보드 상단 요약 카드용)
 * 사용자의 기간별 학습 성과를 요약하여 제공하는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceCard {
    
    private String userId;                  // 사용자 ID
    private LocalDate periodStart;          // 조회 기간 시작일
    private LocalDate periodEnd;            // 조회 기간 종료일
    private Integer totalSessions;          // 해당 기간 내 완료한 총 세션 수
    private Integer totalQuestions;         // 해당 기간 내 풀어본 총 문제 수
    private Integer totalCorrectAnswers;    // 해당 기간 내 맞힌 총 문제 수
    private Double overallAccuracyRate;     // 해당 기간 내 전체 정답률 (%)
    private Integer totalStudyTime;         // 해당 기간 내 총 학습 시간 (분 단위)
    private Double averageSessionTime;      // 세션당 평균 학습 시간 (분 단위)
    private Double totalScore;              // 해당 기간 내 총 획득 점수 (정답 수 × 5점)
    private Integer studyDays;              // 해당 기간 내 실제 학습한 일수
}
