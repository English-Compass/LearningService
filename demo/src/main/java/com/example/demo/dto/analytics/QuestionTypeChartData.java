package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 문제 유형별 성과 차트 데이터 DTO (도넛/바 차트용)
 * 문제 유형별 학습 성과를 차트로 표현하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTypeChartData {
    
    private String questionType;            // 문제 유형 코드 (FILL_IN_THE_BLANK 등)
    private String displayName;             // 사용자에게 표시될 한글명 ("빈칸 채우기" 등)
    private Integer totalQuestions;         // 해당 유형의 총 문제 수
    private Integer correctAnswers;         // 해당 유형의 정답 수
    private Integer wrongAnswers;           // 해당 유형의 오답 수
    private Double accuracyRate;            // 해당 유형의 정답률 (%)
    private Double percentage;              // 전체 문제 중 해당 유형이 차지하는 비율 (%)
    private Double score;                   // 해당 유형에서 획득한 점수 (정답 수 × 5점)
    private String performanceLevel;        // 성과 레벨 (EXCELLENT, GOOD, AVERAGE, POOR)
}
