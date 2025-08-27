package com.example.demo.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학습 패턴 분석 DTO - 문제 할당 목적
 * 
 * Problem Service에서 복습 세션과 오답 세션에 문제를 할당할 때
 * 필요한 핵심적인 학습 패턴 정보만을 제공합니다.
 * 
 * @author Learning Service Team
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPatternAnalysisDTO {
    
    /** 분석 타입 (SESSION_ANALYSIS: 개별 세션, COMPLETE_ANALYSIS: 전체 학습) */
    private String analysisType;
    
    /** 사용자 ID */
    private String userId;
    
    /** 세션 ID (전체 분석의 경우 null) */
    private String sessionId;
    
    /** 분석 수행 시간 */
    private LocalDateTime analyzedAt;
    
    // === 문제 할당을 위한 핵심 분석 내용 ===
    
    /** 문제 유형별 성과 분석 - 문제 할당의 핵심 근거 */
    private List<QuestionTypePerformance> questionTypePerformances;
    
    /** 복습이 필요한 문제 유형들 (정답률 60% 미만) */
    private List<String> reviewRequiredTypes;
    
    /** 개선이 필요한 문제 유형들 (정답률 60-80%) */
    private List<String> improvementRequiredTypes;
    
    /** 강점 영역 문제 유형들 (정답률 80% 이상) */
    private List<String> strengthTypes;
    
    /** 최근 오답한 문제 ID 목록 (최근 1-2주 내) */
    private List<String> recentWrongQuestionIds;
    
    /** 학습 간격이 긴 문제 유형들 (오래 전에 학습한 내용) */
    private List<String> longIntervalTypes;
    
    /** 문제 풀이 시간이 긴 문제 유형들 (이해도 부족 의심) */
    private List<String> slowSolvingTypes;
    
    // === 문제 할당 우선순위 결정을 위한 추가 정보 ===
    
    /** 전체 정답률 */
    private Double overallAccuracyRate;
    
    /** 평균 문제 풀이 시간 (초) */
    private Double averageSolvingTime;
    
    /** 학습 빈도 (DAILY: 매일, WEEKLY: 주간, OCCASIONAL: 가끔) */
    private String studyFrequency;
    
    /** 선호 학습 시간대 (MORNING: 오전, AFTERNOON: 오후, EVENING: 저녁, NIGHT: 밤) */
    private String preferredStudyTime;
}
