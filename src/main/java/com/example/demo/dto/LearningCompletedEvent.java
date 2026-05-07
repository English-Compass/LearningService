package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학습 완료 이벤트 DTO
 * ProblemService SessionCompletedEventDto와 구조를 맞춤
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningCompletedEvent {

    private String eventType;
    private String sessionId;
    private String userId;
    private SessionType sessionType;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime eventTimestamp;

    // ── 학습 결과 (REST 재호출 불필요) ─────────────────────────────────

    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;

    private List<QuestionAnswerData> answers;

    public enum SessionType {
        PRACTICE,
        REVIEW,
        WRONG_ANSWER
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionAnswerData {
        private String questionId;
        private String questionType;
        private String majorCategory;
        private String minorCategory;
        private Integer difficultyLevel;
        private String userAnswer;
        private Boolean isCorrect;
        private Integer timeSpent;
        private LocalDateTime answeredAt;
        private Integer solveCount;
    }
}