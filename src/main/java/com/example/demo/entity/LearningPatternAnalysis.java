package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학습 패턴 분석 결과를 저장하는 통합 엔티티
 * 개별 세션 분석과 전체 학습 분석을 모두 저장할 수 있음
 * analysisType 필드로 분석 유형을 구분
 */
@Entity
@Table(name = "learning_pattern_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPatternAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String analysisId;

    /**
     * 분석 유형
     * SESSION_ANALYSIS: 개별 세션 분석
     * COMPLETE_ANALYSIS: 전체 학습 분석
     * INTEGRATED_ANALYSIS: 통합 분석
     */
    @Column(name = "analysis_type", nullable = false)
    private String analysisType;

    /**
     * 분석 대상 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 분석 대상 세션 ID (전체 분석의 경우 null)
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * 분석 시작 날짜 (전체 분석의 경우)
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * 분석 종료 날짜 (전체 분석의 경우)
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * 기본 통계 정보 (JSON 형태로 저장)
     * - totalQuestions: 총 문제 수
     * - correctAnswers: 정답 수
     * - wrongAnswers: 오답 수
     * - accuracyRate: 정답률
     * - totalDuration: 총 소요시간
     * - averageTimePerQuestion: 문제당 평균 소요시간
     */
    @Column(name = "basic_statistics", columnDefinition = "TEXT")
    private String basicStatistics;

    /**
     * 문제 유형별 성과 정보 (JSON 형태로 저장)
     * - questionType: 문제 유형
     * - totalQuestions: 해당 유형 문제 수
     * - correctAnswers: 해당 유형 정답 수
     * - accuracyRate: 해당 유형 정답률
     * - averageTimeSpent: 해당 유형 평균 소요시간
     */
    @Column(name = "question_type_performances", columnDefinition = "TEXT")
    private String questionTypePerformances;

    /**
     * 학습 패턴 정보 (JSON 형태로 저장)
     * - learningSpeed: 학습 속도 (FAST, NORMAL, SLOW)
     * - consistency: 학습 일관성 (HIGH, MODERATE, LOW)
     * - focusLevel: 집중도 (HIGH, MODERATE, LOW)
     */
    @Column(name = "learning_pattern", columnDefinition = "TEXT")
    private String learningPattern;

    /**
     * 성과 분석 정보 (JSON 형태로 저장)
     * - weakQuestionTypes: 취약한 문제 유형들
     * - strongQuestionTypes: 강점 영역 문제 유형들
     * - improvementAreas: 개선 가능 영역 문제 유형들
     */
    @Column(name = "performance_analysis", columnDefinition = "TEXT")
    private String performanceAnalysis;

    /**
     * 전체 학습 진행도 정보 (전체 분석의 경우만, JSON 형태로 저장)
     * - totalSessions: 총 세션 수
     * - completedSessions: 완료된 세션 수
     * - completionRate: 완료율
     * - weeklyProgress: 주차별 진행도
     */
    @Column(name = "learning_progress", columnDefinition = "TEXT")
    private String learningProgress;

    /**
     * 전체 학습 패턴 정보 (전체 분석의 경우만, JSON 형태로 저장)
     * - learningFrequency: 학습 빈도
     * - timePattern: 시간대별 패턴
     * - learningPersistence: 학습 지속성
     */
    @Column(name = "learning_pattern_info", columnDefinition = "TEXT")
    private String learningPatternInfo;

    /**
     * 분석 수행 시간
     */
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
}
