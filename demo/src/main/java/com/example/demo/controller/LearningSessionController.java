package com.example.demo.controller;

import com.example.demo.dto.LearningSessionDto;
import com.example.demo.service.LearningSessionService;
import com.example.demo.service.LearningServiceOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/learning-sessions")
@RequiredArgsConstructor
@Slf4j
public class LearningSessionController {

    private final LearningServiceOrchestrator learningServiceOrchestrator;

    /**
     * 학습 세션 시작 (개인화된 문제 할당)
     * POST /api/learning-sessions/start
     */
    @PostMapping("/start")
    public ResponseEntity<LearningSessionDto.SessionResponse> startLearningSession(
            @RequestBody StartSessionRequest request) {
        
        log.info("학습 세션 시작 요청: userId={}, sessionType={}, totalQuestions={}", 
            request.getUserId(), request.getSessionType(), request.getTotalQuestions());
        
        LearningSessionDto.SessionResponse session = learningServiceOrchestrator.startPersonalizedLearningSession(
            request.getUserId(),
            request.getSessionType(),
            request.getTotalQuestions()
        );
        
        return ResponseEntity.ok(session);
    }


    /**
     * 학습 세션 진행 상황 조회
     * GET /api/learning-sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<LearningSessionDto.SessionResponse> getLearningSession(@PathVariable String sessionId) {
        log.info("학습 세션 조회: sessionId={}", sessionId);
        
        LearningSessionDto.SessionResponse session = learningServiceOrchestrator.getLearningSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * 사용자의 학습 세션 목록 조회
     * GET /api/learning-sessions/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<LearningSessionDto.SessionListResponse>> getUserLearningSessions(
            @PathVariable String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limit) {
        
        log.info("사용자 학습 세션 목록 조회: userId={}, status={}, limit={}", 
            userId, status, limit);
        
        List<LearningSessionDto.SessionListResponse> sessions = learningServiceOrchestrator.getUserLearningSessions(
            userId, status, limit);
        
        return ResponseEntity.ok(sessions);
    }

    /**
     * 문제 답변 제출
     * POST /api/learning-sessions/{sessionId}/questions
     */
    @PostMapping("/{sessionId}/questions")
    public ResponseEntity<QuestionAnswerResponse> submitQuestionAnswer(
            @PathVariable String sessionId,
            @RequestBody QuestionAnswerRequest request) {
        
        log.info("문제 답변 제출: sessionId={}, questionId={}", 
            sessionId, request.getQuestionId());
        
        // 문제 답변 처리 (오케스트레이터를 통해 처리)
        learningServiceOrchestrator.processQuestionAnswer(
            sessionId, request.getQuestionId(), 
            "A".equals(request.getUserAnswer()), // 임시 정답 처리
            request.getTimeSpent(), request.getUserNotes());
        
        // 임시 응답 생성
        QuestionAnswerResponse response = QuestionAnswerResponse.builder()
            .questionId(request.getQuestionId())
            .isCorrect("A".equals(request.getUserAnswer()))
            .correctAnswer("A")
            .explanation("정답입니다!")
            .earnedPoints("A".equals(request.getUserAnswer()) ? 10 : 0)
            .currentScore(0)
            .progressPercentage(0)
            .nextQuestionId("q2")
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 학습 세션 완료
     * POST /api/learning-sessions/{sessionId}/complete
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<LearningSessionDto.SessionResponse> completeLearningSession(
            @PathVariable String sessionId) {
        
        log.info("학습 세션 완료: sessionId={}", sessionId);
        
        LearningSessionDto.SessionResponse event = learningServiceOrchestrator.completeLearningSession(sessionId);
        return ResponseEntity.ok(event);
    }

    /**
     * 학습 세션 중단
     * POST /api/learning-sessions/{sessionId}/abandon
     */
    @PostMapping("/{sessionId}/abandon")
    public ResponseEntity<LearningSessionDto.SessionResponse> abandonLearningSession(@PathVariable String sessionId) {
        log.info("학습 세션 중단: sessionId={}", sessionId);
        
        LearningSessionDto.SessionResponse session = learningServiceOrchestrator.abandonLearningSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * 학습 세션 통계 조회
     * GET /api/learning-sessions/{sessionId}/statistics
     */
    @GetMapping("/{sessionId}/statistics")
    public ResponseEntity<LearningSessionService.SessionStatistics> getSessionStatistics(@PathVariable String sessionId) {
        log.info("학습 세션 통계 조회: sessionId={}", sessionId);
        
        LearningSessionService.SessionStatistics statistics = learningServiceOrchestrator.getSessionStatistics(sessionId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 학습 세션 재개
     * POST /api/learning-sessions/{sessionId}/resume
     */
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<LearningSessionDto.SessionResponse> resumeLearningSession(@PathVariable String sessionId) {
        log.info("학습 세션 재개: sessionId={}", sessionId);
        
        LearningSessionDto.SessionResponse session = learningServiceOrchestrator.resumeLearningSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * 학습 세션 일시정지
     * POST /api/learning-sessions/{sessionId}/pause
     */
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<LearningSessionDto.SessionResponse> pauseLearningSession(@PathVariable String sessionId) {
        log.info("학습 세션 일시정지: sessionId={}", sessionId);
        
        LearningSessionDto.SessionResponse session = learningServiceOrchestrator.pauseLearningSession(sessionId);
        return ResponseEntity.ok(session);
    }

    /**
     * 현재 진행 중인 세션 조회
     * GET /api/learning-sessions/users/{userId}/current
     */
    @GetMapping("/users/{userId}/current")
    public ResponseEntity<LearningSessionDto.SessionResponse> getCurrentSession(@PathVariable String userId) {
        log.info("현재 진행 중인 세션 조회: userId={}", userId);
        
        LearningSessionDto.SessionResponse session = learningServiceOrchestrator.getCurrentSession(userId);
        return ResponseEntity.ok(session);
    }

    // ===== DTO 클래스들 =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartSessionRequest {
        private String userId;
        private String sessionType; // PRACTICE, REVIEW, WRONG_ANSWER
        private Integer totalQuestions; // 총 문제 수 (기본값: 10)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswerRequest {
        private String questionId;
        private String userAnswer; // A, B, C
        private Integer timeSpent; // 문제 풀이 시간 (초)
        private String userNotes; // 사용자 메모
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswerResponse {
        private String questionId;
        private Boolean isCorrect;
        private String correctAnswer;
        private String explanation;
        private Integer earnedPoints;
        private Integer currentScore;
        private Integer progressPercentage;
        private String nextQuestionId; // 다음 문제 ID (있는 경우)
    }


}
