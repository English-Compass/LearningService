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
    private String sessionType; // 세션 타입 (PRACTICE, REVIEW, WRONG_ANSWER)
    private String difficultyLevel; // 난이도 레벨
    private Map<String, Object> selectedCategories; // 선택된 카테고리
    private Integer totalQuestions; // 총 문제 수
    private LocalDateTime startedAt; // 세션 시작 시간
    private String eventType = "SESSION_STARTED";
}
