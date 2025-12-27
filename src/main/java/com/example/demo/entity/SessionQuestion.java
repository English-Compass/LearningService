package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 세션에 할당된 문제 엔티티
 * 하나의 세션에 여러 문제가 할당되는 관계를 관리
 */
@Entity
@Table(name = "session_question")
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
    
    
    // === 관계 매핑 제거 (성능 및 매핑 충돌 방지) ===
    // 필요시 sessionId, questionId로 개별 조회하여 사용
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "session_id", insertable = false, updatable = false)
    // private LearningSession learningSession;
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "question_id", insertable = false, updatable = false)
    // private Question question;
    
    // === 문제 유형 Enum ===
    public enum QuestionType {
        FILL_IN_THE_BLANK("빈칸 채우기"),
        IDIOM_IN_CONTEXT("문장 속 특정 숙어"),
        PHONETIC_SYMBOL_FINDING("Phonetic Symbol Finding");
        
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
    
    
}
