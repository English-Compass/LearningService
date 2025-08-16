package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSession {
    @Id
    private String sessionId; // 기본키로 사용

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String learningItemId; // 학습 항목 ID

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Integer totalQuestions = 10; // 기본값 10으로 설정

    @Column(nullable = false)
    private Integer answeredQuestions = 0; // 기본값 0으로 설정

    @Column(nullable = false)
    private Integer correctAnswers = 0; // 기본값 0으로 설정

    @Column(nullable = false)
    private Integer wrongAnswers = 0; // 기본값 0으로 설정

    @Column
    private Integer score; // 점수 (백분율)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status; // 기본값 제거, 명시적으로 설정

    @Column(columnDefinition = "TEXT")
    private String sessionMetadata; // 세션 메타데이터 (JSON)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType; // 세션 타입 (PRACTICE, REVIEW, WRONG_ANSWER)

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt; // 마지막 업데이트 시간

    public enum SessionType {
        /**
         * 문제 세션 (PRACTICE)
         * - 새로운 문제를 풀어보는 일반적인 학습 세션
         * - 사용자 레벨과 선호도에 맞춘 문제 할당
         * - 적응형 난이도 조절
         */
        PRACTICE("문제 세션", "새로운 문제를 풀어보는 일반적인 학습 세션"),
        
        /**
         * 복습 세션 (REVIEW)
         * - 이전에 학습한 내용을 복습하는 세션
         * - 학습한 지 오래된 문제들을 우선적으로 할당
         * - 기억 강화를 위한 반복 학습
         */
        REVIEW("복습 세션", "이전에 학습한 내용을 복습하는 세션"),
        
        /**
         * 오답 세션 (WRONG_ANSWER)
         * - 틀린 문제들을 집중적으로 연습하는 세션
         * - 사용자의 취약 영역을 집중 공략
         * - 오답 패턴 분석을 통한 맞춤형 문제 제공
         */
        WRONG_ANSWER("오답 세션", "틀린 문제들을 집중적으로 연습하는 세션");
        
        private final String displayName;
        private final String description;
        
        SessionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * 세션 타입별 기본 문제 수 반환
         */
        public int getDefaultQuestionCount() {
            switch (this) {
                case PRACTICE:
                    return 10; // 일반 문제 세션은 10문제
                case REVIEW:
                    return 15; // 복습 세션은 15문제 (더 많은 복습)
                case WRONG_ANSWER:
                    return 8;  // 오답 세션은 8문제 (집중 연습)
                default:
                    return 10;
            }
        }
        
        /**
         * 세션 타입별 권장 시간(분) 반환
         */
        public int getRecommendedTimeMinutes() {
            switch (this) {
                case PRACTICE:
                    return 20; // 일반 문제 세션은 20분
                case REVIEW:
                    return 25; // 복습 세션은 25분
                case WRONG_ANSWER:
                    return 15; // 오답 세션은 15분 (집중)
                default:
                    return 20;
            }
        }
        
        /**
         * 세션 타입별 힌트 사용 가능 여부
         */
        public boolean isHintEnabled() {
            switch (this) {
                case PRACTICE:
                    return true;  // 일반 문제 세션은 힌트 사용 가능
                case REVIEW:
                    return false; // 복습 세션은 힌트 사용 불가
                case WRONG_ANSWER:
                    return true;  // 오답 세션은 힌트 사용 가능
                default:
                    return true;
            }
        }
    }

    public enum SessionStatus {
        STARTED,        // 세션 시작
        IN_PROGRESS,    // 진행 중
        PAUSED,         // 일시정지
        COMPLETED,      // 완료
        ABANDONED       // 중단
    }

    // 점수 계산 메서드
    public void calculateScore() {
        if (totalQuestions > 0) {
            this.score = (int) Math.round((double) correctAnswers / totalQuestions * 100);
        }
    }

    // 세션 시작 처리
    public void startSession() {
        this.status = SessionStatus.STARTED;
        this.startedAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // 세션 진행 중으로 변경
    public void setInProgress() {
        this.status = SessionStatus.IN_PROGRESS;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // 세션 완료 처리
    public void completeSession() {
        this.completedAt = LocalDateTime.now();
        this.status = SessionStatus.COMPLETED;
        this.lastUpdatedAt = LocalDateTime.now();
        calculateScore();
    }

    // 세션 중단 처리
    public void abandonSession() {
        this.status = SessionStatus.ABANDONED;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // 문제 답변 업데이트
    public void updateQuestionAnswer(boolean isCorrect) {
        this.answeredQuestions++;
        if (isCorrect) {
            this.correctAnswers++;
        } else {
            this.wrongAnswers++;
        }
        this.lastUpdatedAt = LocalDateTime.now();
        
        // 첫 번째 문제 답변 시 진행 중으로 상태 변경
        if (this.answeredQuestions == 1) {
            this.status = SessionStatus.IN_PROGRESS;
        }
    }

    // 진행률 계산
    public double getProgressPercentage() {
        if (totalQuestions > 0) {
            return (double) answeredQuestions / totalQuestions * 100;
        }
        return 0.0;
    }

    // 정답률 계산
    public double getAccuracyPercentage() {
        if (answeredQuestions > 0) {
            return (double) correctAnswers / answeredQuestions * 100;
        }
        return 0.0;
    }
}
