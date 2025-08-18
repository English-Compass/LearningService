package com.example.demo.dto;

import com.example.demo.entity.LearningSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학습 세션 응답용 DTO
 * 엔티티를 외부에 노출하지 않고 필요한 정보만 전달
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSessionResponseDto {
    
    private String sessionId;
    private String userId;
    private String learningItemId;
    private String status;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer score;
    private Integer totalDuration;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastUpdatedAt;
    
    /**
     * 엔티티를 DTO로 변환
     */
    public static LearningSessionResponseDto from(LearningSession session) {
        return LearningSessionResponseDto.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .learningItemId(session.getLearningItemId())
            .status(session.getStatus().name())
            .totalQuestions(session.getTotalQuestions())
            .answeredQuestions(session.getAnsweredQuestions())
            .correctAnswers(session.getCorrectAnswers())
            .wrongAnswers(session.getWrongAnswers())
            .score(session.getScore())
            .totalDuration(session.getTotalDuration())
            .startedAt(session.getStartedAt())
            .completedAt(session.getCompletedAt())
            .lastUpdatedAt(session.getLastUpdatedAt())
            .build();
    }
}
