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
    private String sessionId; // LearningSession 참조
    
    @Column(nullable = false)
    private String questionId; // ProblemService 문제 ID (외래키 없음)

    @Column(nullable = false)
    private String sessionType; // 세션 타입 (PRACTICE, REVIEW, WRONG_ANSWER)
    
    // === ProblemService에서 받은 문제 메타데이터 (분석용) ===
    @Column(name = "question_type")
    private String questionType; // 문제 유형 (VOCABULARY, GRAMMAR, READING 등)
    
    @Column(name = "major_category")
    private String majorCategory; // 대분류 (READING, LISTENING 등)
    
    @Column(name = "minor_category")
    private String minorCategory; // 소분류 (WORD_USAGE, SENTENCE_STRUCTURE 등)
    
    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 난이도 (1~3)
    
    // === 사용자 답변 데이터 ===
    @Column(nullable = false)
    private String userAnswer; // 사용자 답변 (A, B, C 중 하나)
    
    @Column(nullable = false)
    private Boolean isCorrect; // 정답 여부 (ProblemService에서 계산된 값)
    
    @Column
    private Integer timeSpent; // 문제 풀이 시간(초)
    
    @Column(nullable = false)
    private LocalDateTime answeredAt; // 답변 시간

    @Column(nullable = false)
    private Integer solveCount; // 특정 문제 풀이 횟수(집계 자료)
}
