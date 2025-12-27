package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 주간 학습 추이 데이터 DTO (라인 차트용)
 * 주별 학습 성과 추이를 그래프로 표현하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyTrendData {
    
    private LocalDate weekStartDate;        // 해당 주의 시작일 (월요일)
    private LocalDate weekEndDate;          // 해당 주의 종료일 (일요일)
    private String weekLabel;               // 그래프 X축 레이블용 ("3월 1일주" 등)
    private Integer sessionsCompleted;      // 해당 주에 완료한 세션 수
    private Integer questionsAnswered;      // 해당 주에 풀어본 문제 수
    private Integer correctAnswers;         // 해당 주에 맞힌 문제 수
    private Double accuracyRate;            // 해당 주의 정답률 (%) - 라인 차트의 주요 지표
    private Integer studyTimeMinutes;       // 해당 주의 총 학습 시간 (분 단위)
    private Double averageScore;            // 해당 주의 획득 점수 (정답 수 × 5점)
}
