package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 문제 유형별 성과 통계 DTO
 * 
 * 사용자의 특정 문제 유형(빈칸 채우기, 동의어 선택 등)에 대한
 * 상세 성과 분석 데이터를 담는 클래스입니다.
 * 
 * @author Learning Service Team
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTypePerformance {
    
    /** 문제 유형 코드 (예: FILL_IN_THE_BLANK, SYNONYM_SELECTION) */
    private String questionType;
    
    /** 문제 유형 표시명 (예: "빈칸 채우기", "동의어 선택") */
    private String displayName;
    
    /** 해당 유형의 총 문제 수 */
    private Integer totalQuestions;
    
    /** 정답 수 */
    private Integer correctAnswers;
    
    /** 정답률 (%) - 0.0 ~ 100.0 범위 */
    private Double accuracyRate;
    
    /** 문제당 평균 풀이 시간 (초) */
    private Double averageTime;
    
    /** 성과 레벨 (EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT) */
    private String performanceLevel;
}
