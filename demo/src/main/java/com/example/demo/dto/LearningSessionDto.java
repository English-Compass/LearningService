package com.example.demo.dto;

import com.example.demo.entity.LearningSession;
import com.example.demo.entity.QuestionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * LearningSession 엔티티 기반 DTO 클래스들
 */
public class LearningSessionDto {

    /**
     * 학습 세션 생성 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSessionRequest {
        private String userId;
        private String sessionType;           // learningItemId → sessionType으로 통일
        private String difficultyLevel;
        private Map<String, List<String>> selectedCategories;
    }

    /**
     * 학습 세션 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResponse {
        private String sessionId;
        private String userId;
        private String sessionType;           // learningItemId → sessionType으로 통일
        private String status;
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private Integer correctAnswers;       // 추가
        private Integer wrongAnswers;         // 추가
        private Double score;                 // 점수 추가
        private Double progressPercentage;
        private LocalDateTime startedAt;      // 추가
        private LocalDateTime lastUpdatedAt;  // 추가
    }

    /**
     * 학습 세션 업데이트 요청 DTO
     * LearningSession.updateQuestionAnswer() 메서드와 연관
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSessionRequest {
        private String sessionId;
        private Boolean isCorrect; // updateQuestionAnswer() 파라미터
        private Integer timeSpent; // QuestionAnswer.timeSpent
        private String userNotes; // QuestionAnswer.userNotes
    }

    /**
     * 학습 세션 통계 DTO
     * LearningSession의 통계 관련 필드들
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionStatistics {
        private String sessionId;
        private String userId;
        
        // 진행 상황 통계
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private Integer correctAnswers;
        private Integer wrongAnswers;
        private Double score;
        
        // 시간 통계
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long totalDurationSeconds; // 계산된 값
        
        // 비율 통계 (LearningSession 메서드 기반)
        private Double progressPercentage;
        private Double accuracyPercentage;
        
        // 상태 정보
        private LearningSession.SessionStatus status;
        private LocalDateTime lastUpdatedAt;
    }

    /**
     * 학습 세션 목록 조회 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionListResponse {
        private String sessionId;
        private String learningItemId;
        private String sessionType;  // sessionType 필드 추가
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private Double score;
        private LearningSession.SessionStatus status;
        private Double progressPercentage;
        private Double accuracyPercentage;
    }
}
