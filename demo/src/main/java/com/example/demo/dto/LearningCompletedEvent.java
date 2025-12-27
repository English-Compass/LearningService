package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학습 완료 이벤트 DTO
 * ProblemService에서 발행하는 SessionCompletedEventDto와 동일한 구조
 * Kafka 메시징을 통해 세션 완료 이벤트 정보를 수신하는 역할
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드는 무시
public class LearningCompletedEvent {
    
    // 이벤트 유형 (예: "SESSION_COMPLETED")
    private String eventType;
    
    // 완료된 학습 세션의 고유 식별자
    private String sessionId;
    
    // 세션을 완료한 사용자의 ID
    private String userId;
    
    // 완료된 세션의 유형 (PRACTICE, REVIEW, WRONG_ANSWER)
    private SessionType sessionType;
    
    // 세션 완료 시간
    private LocalDateTime completedAt;
    
    // 이벤트 발생 시간 (현재 시간)
    private LocalDateTime eventTimestamp;
    
    /**
     * 세션 타입 Enum
     * ProblemService의 SessionType과 동일한 구조
     */
    public enum SessionType {
        PRACTICE,
        REVIEW,
        WRONG_ANSWER
    }
}