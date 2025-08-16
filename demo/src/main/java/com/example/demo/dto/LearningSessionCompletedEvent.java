package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSessionCompletedEvent {
    
    private String sessionId;
    private String userId;
    private String learningItemId;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer score;
    private Integer totalDuration;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Map<String, Object> sessionMetadata;
    private String eventType = "SESSION_COMPLETED";
    private LocalDateTime timestamp;
}
