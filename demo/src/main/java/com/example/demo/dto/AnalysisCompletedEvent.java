package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 분석 완료 이벤트 DTO
 * ProblemService에서 구독할 이벤트 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisCompletedEvent {
    
    // 이벤트 유형
    @Builder.Default
    private String eventType = "ANALYSIS_COMPLETED";
    
    // 사용자 ID
    private String userId;
    
    // 세션 ID
    private String sessionId;
    
    // 개별 세션 분석 ID
    private String sessionAnalysisId;
    
    // 전체 학습 분석 ID
    private String completeAnalysisId;
    
    // 분석 완료 시간
    private LocalDateTime completedAt;
    
    // 추가 메타데이터
    private Map<String, Object> metadata;
}

