package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 학습 완료 이벤트 DTO
 * 전체 학습 결과 분석 및 오답/복습 문제 제공을 위한 데이터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningCompletedEvent {
    
    // 이벤트 기본 정보
    private String eventId;
    private LocalDateTime timestamp;
    
    // 세션 식별 정보
    private String sessionId;
    private String userId;
    
    // 이벤트 메타데이터
    private String eventType = "SESSION_COMPLETED";
    private String eventSource = "Problem_Service";
    
    // 추가 메타데이터 (필요시)
    private Map<String, Object> metadata;
}