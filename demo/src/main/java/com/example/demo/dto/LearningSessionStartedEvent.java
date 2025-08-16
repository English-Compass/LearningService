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
public class LearningSessionStartedEvent {
    
    private String sessionId;
    private String userId;
    private String learningItemId;
    private String sessionType;
    private String difficultyLevel;
    private Map<String, Object> selectedCategories;
    private Integer customQuestionCount;
    private LocalDateTime startedAt;
    private String eventType = "SESSION_STARTED";
}
