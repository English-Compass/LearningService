package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 일별 학습 활동 데이터 DTO (캘린더 히트맵용)
 * 일별 학습 활동 강도를 히트맵으로 표현하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyActivityData {
    
    private String date;                    // 날짜 문자열 (YYYY-MM-DD 형식)
    private Integer questionsAnswered;      // 해당 날짜에 풀어본 문제 수
    private Integer correctAnswers;         // 해당 날짜에 맞힌 문제 수
    private Double accuracyRate;            // 해당 날짜의 정답률 (%)
    private Integer studyTimeMinutes;       // 해당 날짜의 학습 시간 (분 단위)
    private Integer activityLevel;          // 활동 강도 레벨 (0-4, 히트맵 색상용)
    private Boolean hasActivity;            // 학습 활동 여부 (boolean)
}
