package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_answers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sessionId; // LearningSession 참조
    
    @Column(nullable = false)
    private String questionId; // Question 참조 (외래키)
    
    // === 사용자 답변 데이터만 ===
    @Column(nullable = false)
    private String userAnswer; // 사용자 답변 (A, B, C 중 하나)
    
    @Column(nullable = false)
    private Boolean isCorrect; // 정답 여부 (계산된 값)
    
    @Column
    private Integer timeSpent; // 문제 풀이 시간(초)
    
    @Column(nullable = false)
    private LocalDateTime answeredAt; // 답변 시간
    
    @Column(columnDefinition = "TEXT")
    private String userNotes; // 사용자 메모
    
    // === 관계 매핑 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private LearningSession learningSession;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;
    
    // === 편의 메서드들 ===
    
    // 정답 여부 자동 계산
    public void calculateCorrectness() {
        if (question != null && userAnswer != null) {
            this.isCorrect = userAnswer.equals(question.getCorrectAnswer());
        }
    }
    
    // 사용자 답변 텍스트 조회 (Question에서 가져옴)
    public String getUserAnswerText() {
        if (question != null && userAnswer != null) {
            switch (userAnswer.toUpperCase()) {
                case "A": return question.getOptionA();
                case "B": return question.getOptionB();
                case "C": return question.getOptionC();
                default: return null;
            }
        }
        return null;
    }
    
    // 정답 텍스트 조회 (Question에서 가져옴)
    public String getCorrectAnswerText() {
        if (question != null) {
            switch (question.getCorrectAnswer().toUpperCase()) {
                case "A": return question.getOptionA();
                case "B": return question.getOptionB();
                case "C": return question.getOptionC();
                default: return null;
            }
        }
        return null;
    }
    
    // 문제 해설 조회 (Question에서 가져옴)
    public String getExplanation() {
        return question != null ? question.getExplanation() : null;
    }
    
    // 문제 난이도 조회 (Question에서 가져옴)
    public Integer getDifficulty() {
        return question != null ? question.getDifficulty() : null;
    }
    
    // 문제 대분류 조회 (Question에서 가져옴)
    public String getMajorCategory() {
        return question != null ? question.getMajorCategory().name() : null;
    }
    
    // 문제 소분류 조회 (Question에서 가져옴)
    public String getMinorCategory() {
        return question != null ? question.getMinorCategory().name() : null;
    }
    
    // 문제 유형 조회 (Question에서 가져옴)
    public String getQuestionType() {
        return question != null ? question.getQuestionType().getDisplayName() : null;
    }
    
    // 문제 텍스트 조회 (Question에서 가져옴)
    public String getQuestionText() {
        return question != null ? question.getQuestionText() : null;
    }
}
