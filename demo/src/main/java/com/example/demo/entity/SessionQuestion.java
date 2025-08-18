package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션에 할당된 문제 엔티티
 * 하나의 세션에 여러 문제가 할당되는 관계를 관리
 */
@Entity
@Table(name = "session_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 자동 생성되는 고유 ID
    
    @Column(nullable = false)
    private String sessionId; // 학습 세션 ID
    
    @Column(nullable = false)
    private String questionId; // 문제 ID
    
    @Column(nullable = false)
    private Integer questionOrder; // 문제 순서 (1, 2, 3, 4, 5...)
    
    @Column(nullable = false)
    private String majorCategory; // 문제 대분류 (STUDY, BUSINESS, TRAVEL, DAILY_LIFE)
    
    @Column(nullable = false)
    private String minorCategory; // 문제 소분류
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType; // 문제 유형 (Enum 사용)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty; // 문제 난이도 (Enum 사용)
    
    @Column(nullable = false)
    private LocalDateTime assignedAt; // 할당 시간
    
    @Column
    private LocalDateTime answeredAt; // 답변 완료 시간
    
    @Column
    private Boolean isAnswered; // 답변 완료 여부
    
    @Column
    private Boolean isCorrect; // 정답 여부 (답변 완료 시)
    
    @Column
    private Integer timeSpent; // 문제 풀이 시간 (초)
    
    @Column
    private String userAnswer; // 사용자 답변
    
    @Column
    private String correctAnswer; // 정답
    
    @Column(columnDefinition = "TEXT")
    private String explanation; // 문제 해설
    
    @Column(columnDefinition = "TEXT")
    private String userNotes; // 사용자 메모
    
    @Column(columnDefinition = "JSON")
    private String questionMetadata; // 문제 메타데이터 (JSON)
    
    // === 관계 매핑 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private LearningSession learningSession;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
    
    // === 문제 유형 Enum ===
    public enum QuestionType {
        FILL_IN_THE_BLANK("빈칸 채우기"),
        IDIOM_IN_CONTEXT("문장 속 특정 숙어"),
        SENTENCE_COMPLETION("문장 완성");
        
        private final String displayName;
        
        QuestionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // === 문제 난이도 Enum ===
    public enum DifficultyLevel {
        BEGINNER("초급", 1),
        INTERMEDIATE("중급", 2),
        ADVANCED("상급", 3);
        
        private final String displayName;
        private final int numericValue;
        
        DifficultyLevel(String displayName, int numericValue) {
            this.displayName = displayName;
            this.numericValue = numericValue;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getNumericValue() {
            return numericValue;
        }
        
        // 숫자 값으로부터 Enum 찾기
        public static DifficultyLevel fromNumericValue(int value) {
            for (DifficultyLevel level : values()) {
                if (level.getNumericValue() == value) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Invalid difficulty value: " + value);
        }
        
        // 다음 난이도 반환
        public DifficultyLevel getNextLevel() {
            switch (this) {
                case BEGINNER: return INTERMEDIATE;
                case INTERMEDIATE: return ADVANCED;
                case ADVANCED: return ADVANCED; // 최고 난이도
                default: return this;
            }
        }
        
        // 이전 난이도 반환
        public DifficultyLevel getPreviousLevel() {
            switch (this) {
                case BEGINNER: return BEGINNER; // 최저 난이도
                case INTERMEDIATE: return BEGINNER;
                case ADVANCED: return INTERMEDIATE;
                default: return this;
            }
        }
        
        // 난이도가 상승했는지 확인
        public boolean isLevelUp(DifficultyLevel previousLevel) {
            return this.getNumericValue() > previousLevel.getNumericValue();
        }
        
        // 난이도가 하락했는지 확인
        public boolean isLevelDown(DifficultyLevel previousLevel) {
            return this.getNumericValue() < previousLevel.getNumericValue();
        }
    }
    
    // === 유틸리티 메서드 ===
    
    // 문제 답변 완료 처리
    public void markAsAnswered(String userAnswer, String correctAnswer, boolean isCorrect, Integer timeSpent) {
        this.isAnswered = true;
        this.answeredAt = LocalDateTime.now();
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.timeSpent = timeSpent;
    }
    
    // 문제 답변 취소 (재시도용)
    public void resetAnswer() {
        this.isAnswered = false;
        this.answeredAt = null;
        this.userAnswer = null;
        this.isCorrect = null;
        this.timeSpent = null;
    }
    
    // 문제 난이도 조정
    public void adjustDifficulty(DifficultyLevel newDifficulty) {
        if (newDifficulty != null) {
            this.difficulty = newDifficulty;
        }
    }
    
    // 문제 난이도 상승
    public void increaseDifficulty() {
        this.difficulty = this.difficulty.getNextLevel();
    }
    
    // 문제 난이도 하락
    public void decreaseDifficulty() {
        this.difficulty = this.difficulty.getPreviousLevel();
    }
    
    // 현재 난이도가 초급인지 확인
    public boolean isBeginnerLevel() {
        return DifficultyLevel.BEGINNER.equals(this.difficulty);
    }
    
    // 현재 난이도가 중급인지 확인
    public boolean isIntermediateLevel() {
        return DifficultyLevel.INTERMEDIATE.equals(this.difficulty);
    }
    
    // 현재 난이도가 상급인지 확인
    public boolean isAdvancedLevel() {
        return DifficultyLevel.ADVANCED.equals(this.difficulty);
    }
    
    // 문제 풀이 시간 업데이트
    public void updateTimeSpent(Integer additionalTime) {
        if (this.timeSpent == null) {
            this.timeSpent = 0;
        }
        this.timeSpent += additionalTime;
    }
}
