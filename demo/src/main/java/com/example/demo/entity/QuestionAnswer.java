package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String sessionId; // LearningSession의 sessionId 참조

    @Column(nullable = false)
    private String questionId; // 문제 ID

    @Column(nullable = false)
    private String questionText; // 문제 내용

    // 보기(선택지) 필드들
    @Column(nullable = false)
    private String optionA; // 보기 A

    @Column(nullable = false)
    private String optionB; // 보기 B

    @Column(nullable = false)
    private String optionC; // 보기 C

    @Column(nullable = false)
    private String userAnswer; // 사용자 답변 (A, B, C 중 하나)

    @Column(nullable = false)
    private String correctAnswer; // 정답 (A, B, C 중 하나)

    @Column(nullable = false)
    private Boolean isCorrect; // 정답 여부

    @Column(nullable = false)
    private Integer pointsPerQuestion; // 문제당 점수 (사용자별 맞춤형)

    @Column(nullable = false)
    private Integer earnedPoints; // 획득한 점수 (정답 시 pointsPerQuestion, 오답 시 0)

    @Column
    private Integer timeSpent; // 문제 풀이 시간(초)

    @Column(nullable = false)
    private LocalDateTime answeredAt; // 답변 시간

    @Column(columnDefinition = "TEXT")
    private String explanation; // 문제 해설

    @Column(columnDefinition = "TEXT")
    private String userNotes; // 사용자 메모

    @Column
    private Integer difficulty; // 문제 난이도 (1-3)

    // 문제 분류 필드들
    @Column(nullable = false)
    private String majorCategory; // 대분류 (STUDY, BUSINESS, TRAVEL, DAILY_LIFE)

    @Column(nullable = false)
    private String minorCategory; // 소분류 (GRAMMAR, VOCABULARY, READING, MEETING, PRESENTATION, NEGOTIATION, AIRPORT, HOTEL, RESTAURANT, SHOPPING, COOKING, TRANSPORTATION)

    @Column(nullable = false)
    private String questionType; // 문제 유형 (FILL_IN_THE_BLANK, IDIOM_IN_CONTEXT, SENTENCE_COMPLETION)

    @Column
    private String tags; // 문제 태그 (JSON 형태)

    // 보기 관련 유틸리티 메서드
    public String getOptionByAnswer(String answer) {
        switch (answer.toUpperCase()) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            default: return null;
        }
    }

    public String getUserAnswerText() {
        return getOptionByAnswer(userAnswer);
    }

    public String getCorrectAnswerText() {
        return getOptionByAnswer(correctAnswer);
    }
}
