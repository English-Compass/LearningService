package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ProblemService에서 발행하는 세션 완료 이벤트와 동일한 구조의 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SessionCompletedEventDto {

    private String eventType;
    private String sessionId;
    private String userId;
    private SessionType sessionType;
    private LocalDateTime completedAt;
    private LocalDateTime eventTimestamp;

    public enum SessionType {
        PRACTICE,
        REVIEW,
        WRONG_ANSWER
    }
}

