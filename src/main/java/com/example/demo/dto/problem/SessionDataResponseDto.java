package com.example.demo.dto.problem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ProblemService API 응답 DTO
 * GET /api/problem/internal/sessions/{sessionId}?userId={userId} 응답 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataResponseDto {
    
    @JsonProperty("session")
    private SessionDto session;
    
    @JsonProperty("questions")
    private List<QuestionAnswerDto> questions;
    
    @JsonProperty("events")
    private List<SessionEventDto> events;
    
    /**
     * 세션 메타데이터 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDto {
        private String sessionId;
        private String userId;
        private String sessionType; // PRACTICE, REVIEW, WRONG_ANSWER
        private String status; // STARTED, IN_PROGRESS, COMPLETED
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Map<String, Object> metadata;
    }
    
    /**
     * 문항 + 사용자 답변 데이터 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswerDto {
        private String questionId;
        private String questionType; // VOCABULARY, GRAMMAR, READING 등
        private String majorCategory; // READING, LISTENING 등
        private String minorCategory; // WORD_USAGE, SENTENCE_STRUCTURE 등
        private Integer difficultyLevel; // 1~3 등급
        private String userAnswer; // 사용자가 선택한 답안
        private Boolean isCorrect; // 정답 여부 (이미 계산된 값)
        private Integer timeSpent; // 풀이 시간 (초 단위)
        private LocalDateTime answeredAt; // 답안 제출 시각
        private Integer solveCount; // 해당 문제 풀이 누적 횟수
        private Map<String, Object> metadata;
    }
    
    /**
     * 세션 이벤트 로그 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionEventDto {
        private String eventId;
        private String eventType; // SESSION_COMPLETED, QUESTION_SKIPPED 등
        private String sessionId;
        private String userId;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
    }
}

