package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_answer")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sessionId; // LearningSession 참조방ㄱ
    
    @Column(nullable = false)
    private String questionId; // Question 참조 (외래키)

    @Column(nullable = false)
    private String sessionType; // 세션 타입 (PRACTICE, REVIEW, WRONG_ANSWER)
    
    // === 사용자 답변 데이터만 ===
    @Column(nullable = false)
    private String userAnswer; // 사용자 답변 (A, B, C 중 하나)
    
    @Column(nullable = false)
    private Boolean isCorrect; // 정답 여부 (계산된 값)
    
    @Column
    private Integer timeSpent; // 문제 풀이 시간(초)
    
    @Column(nullable = false)
    private LocalDateTime answeredAt; // 답변 시간

    @Column(nullable = false)
    private Integer solveCount; // 특정 문제 풀이 횟수(집계 자료)
    
    
    // === 편의 메서드들 ===
    
    // 정답 여부 자동 계산 (Question 객체 필요)
    public void calculateCorrectness(Question question) {
        if (question != null && userAnswer != null) {
            this.isCorrect = userAnswer.equals(question.getCorrectAnswer());
        }
    }
    
    // === 편의 메서드들 (관계 매핑 제거로 인해 Question 객체 파라미터 필요) ===
    
    // 사용자 답변 텍스트 조회 (Question 객체 필요)
    public String getUserAnswerText(Question question) {
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
    
    // 정답 텍스트 조회 (Question 객체 필요)
    public String getCorrectAnswerText(Question question) {
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
    
    // 문제 해설 조회 (Question 객체 필요)
    public String getExplanation(Question question) {
        return question != null ? question.getExplanation() : null;
    }
    
    // 문제 난이도 조회 (Question 객체 필요)
    public String getDifficulty(Question question) {
        return question != null ? question.getDifficultyLevel() : null;
    }
    
    // 문제 대분류 조회 (Question 객체 필요)
    public String getMajorCategory(Question question) {
        return question != null ? question.getMajorCategory().name() : null;
    }
    
    // 문제 소분류 조회 (Question 객체 필요)
    public String getMinorCategory(Question question) {
        return question != null ? question.getMinorCategory().name() : null;
    }
    
    // 문제 유형 조회 (Question 객체 필요)
    public String getQuestionType(Question question) {
        return question != null ? question.getQuestionType().getDisplayName() : null;
    }
    
    // 문제 텍스트 조회 (Question 객체 필요)
    public String getQuestionText(Question question) {
        return question != null ? question.getQuestionText() : null;
    }
}
