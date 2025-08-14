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
public class LearningSummary {
    private String userId;
    private String learningItemId;
    private LocalDateTime firstStartedAt;
    private LocalDateTime lastCompletedAt;
    private Integer totalDuration; // 총 학습 시간(초)
    private Integer completionCount; // 완료 횟수
    private Map<String, Integer> eventTypeCounts; // 이벤트 타입별 횟수
}
