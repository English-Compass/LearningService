package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카테고리별 성과 통계 DTO
 * 
 * 사용자의 특정 카테고리(비즈니스-회의, 학습-문법 등)에 대한
 * 성과 분석 데이터를 담는 클래스입니다.
 * 
 * @author Learning Service Team
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPerformance {
    
    /** 대분류 카테고리 (BUSINESS, STUDY) */
    private String majorCategory;
    
    /** 소분류 카테고리 (MEETING_CONFERENCE, BASIC_GRAMMAR 등) */
    private String minorCategory;
    
    /** 해당 카테고리의 총 문제 수 */
    private Integer totalQuestions;
    
    /** 정답 수 */
    private Integer correctAnswers;
    
    /** 정답률 (%) - 0.0 ~ 100.0 범위 */
    private Double accuracyRate;
    
    /** 주로 푼 난이도 레벨 (초급, 중급, 상급) */
    private String difficultyLevel;
}
