package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * 문제별 통계 정보를 담는 읽기 전용 뷰 엔티티
 * 
 * 이 뷰는 사용자들의 학습 이력을 집계하여 각 문제에 대한 통계 정보를 미리 계산해둔 결과입니다.
 * 주로 문제 유형별 성과 분석, 난이도별 정답률 분석, 사용자별 문제 풀이 패턴 분석 등에 활용됩니다.
 * 
 * 특징:
 * - @Immutable: 읽기 전용으로 설정되어 데이터 수정이 불가능
 * - 실시간 집계가 아닌 배치 작업을 통해 주기적으로 업데이트
 * - 복잡한 JOIN과 집계 쿼리를 미리 실행하여 성능 최적화
 * 
 * 활용 사례:
 * - 학습 패턴 분석 서비스에서 문제 유형별 성과 분석
 * - 개인화된 문제 추천 시스템에서 난이도별 문제 매칭
 * - 학습 진도 관리에서 취약 영역 식별
 */
@Entity
@Immutable
@Table(name = "question_stats_view")
public class QuestionStatsView {

    /**
     * 문제 고유 식별자
     * question 테이블의 기본키와 연결
     */
    @Id
    @Column(name = "question_id")
    private String questionId;

    /**
     * 문제 유형 (예: FILL_IN_THE_BLANK, IDIOM_IN_CONTEXT, GRAMMAR_QUIZ 등)
     * 문제 유형별 성과 분석에 활용
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionCategory.QuestionType questionType;

    /**
     * 문제의 주요 카테고리 (예: VOCABULARY, GRAMMAR, READING 등)
     * 카테고리별 학습 진도 관리에 활용
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private QuestionCategory.MajorCategory category;

    /**
     * 문제 난이도 (1: 쉬움, 2: 보통, 3: 어려움)
     * 난이도별 문제 추천 및 학습자 수준별 문제 매칭에 활용
     */
    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;

    /**
     * 해당 문제를 푼 총 횟수
     * question_answer 테이블의 집계 결과
     */
    @Column(name = "total_solve_count", nullable = false)
    private Long totalSolveCount;

    /**
     * 해당 문제를 정답으로 푼 횟수
     * question_answer 테이블에서 is_correct = true인 레코드 집계
     */
    @Column(name = "correct_solve_count", nullable = false)
    private Long correctSolveCount;

    /**
     * 정답률 (0.00 ~ 1.00)
     * correct_solve_count / total_solve_count로 계산
     * 학습 패턴 분석에서 취약 영역 식별에 활용
     */
    @Column(name = "correct_rate", nullable = false)
    private BigDecimal correctRate;

    /**
     * 사용자당 평균 풀이 시도 횟수
     * total_solve_count / distinct_user_count로 계산
     * 문제의 복잡도나 이해도 측정에 활용
     */
    @Column(name = "avg_solve_attempts_per_user", nullable = false)
    private BigDecimal avgSolveAttemptsPerUser;

    /**
     * 해당 문제를 풀어본 고유 사용자 수
     * question_answer 테이블의 user_id DISTINCT 집계
     * 문제의 인기도나 접근성 측정에 활용
     */
    @Column(name = "distinct_user_count", nullable = false)
    private Long distinctUserCount;

    /**
     * 기본 생성자
     * JPA 엔티티 매핑을 위해 필요
     */
    public QuestionStatsView() {}

    /**
     * 전체 필드를 포함한 생성자
     * 뷰 데이터 생성 시 사용
     * 
     * @param questionId 문제 고유 식별자
     * @param questionType 문제 유형
     * @param category 문제 카테고리
     * @param difficultyLevel 문제 난이도
     * @param totalSolveCount 총 풀이 횟수
     * @param correctSolveCount 정답 횟수
     * @param correctRate 정답률
     * @param avgSolveAttemptsPerUser 사용자당 평균 풀이 시도 횟수
     * @param distinctUserCount 고유 사용자 수
     */
    public QuestionStatsView(String questionId, QuestionCategory.QuestionType questionType, 
                           QuestionCategory.MajorCategory category, Integer difficultyLevel,
                           Long totalSolveCount, Long correctSolveCount, BigDecimal correctRate,
                           BigDecimal avgSolveAttemptsPerUser, Long distinctUserCount) {
        this.questionId = questionId;
        this.questionType = questionType;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
        this.totalSolveCount = totalSolveCount;
        this.correctSolveCount = correctSolveCount;
        this.correctRate = correctRate;
        this.avgSolveAttemptsPerUser = avgSolveAttemptsPerUser;
        this.distinctUserCount = distinctUserCount;
    }

    /**
     * Getter 메서드들
     * 읽기 전용 뷰이므로 Setter는 제공하지 않음
     * 데이터 수정이 필요한 경우 원본 테이블을 수정하고 뷰를 재생성해야 함
     */
    public String getQuestionId() {
        return questionId;
    }

    public QuestionCategory.QuestionType getQuestionType() {
        return questionType;
    }

    public QuestionCategory.MajorCategory getCategory() {
        return category;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public Long getTotalSolveCount() {
        return totalSolveCount;
    }

    public Long getCorrectSolveCount() {
        return correctSolveCount;
    }

    public BigDecimal getCorrectRate() {
        return correctRate;
    }

    public BigDecimal getAvgSolveAttemptsPerUser() {
        return avgSolveAttemptsPerUser;
    }

    public Long getDistinctUserCount() {
        return distinctUserCount;
    }

    /**
     * 객체의 문자열 표현을 반환
     * 디버깅 및 로깅 목적으로 사용
     * 
     * @return 문제 통계 정보를 포함한 문자열
     */
    @Override
    public String toString() {
        return "QuestionStatsView{" +
                "questionId=" + questionId +
                ", questionType=" + questionType +
                ", category=" + category +
                ", difficultyLevel=" + difficultyLevel +
                ", totalSolveCount=" + totalSolveCount +
                ", correctSolveCount=" + correctSolveCount +
                ", correctRate=" + correctRate +
                ", avgSolveAttemptsPerUser=" + avgSolveAttemptsPerUser +
                ", distinctUserCount=" + distinctUserCount +
                '}';
    }
}