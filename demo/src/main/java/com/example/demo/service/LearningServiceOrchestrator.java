package com.example.demo.service;


import com.example.demo.dto.LearningSessionDto;
import com.example.demo.entity.LearningSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 학습 서비스 오케스트레이터
 * 학습 세션 관리, 개인화된 문제 할당, 이벤트 발행을 연결
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningServiceOrchestrator {

    private final LearningSessionService learningSessionService;
    private final EventPublishingService eventPublishingService;
    private final PersonalizedQuestionAssignmentService questionAssignmentService;

    /**
     * 개인화된 학습 세션 시작
     */
    public LearningSessionDto.SessionResponse startPersonalizedLearningSession(String userId, String sessionType, 
                                                         int totalQuestions) {
        log.info("개인화된 학습 세션 시작: userId={}, sessionType={}, totalQuestions={}", 
            userId, sessionType, totalQuestions);
        
        // 1. 사용자 맞춤형 문제 할당
        PersonalizedQuestionAssignmentService.QuestionAssignmentResult assignment = 
            questionAssignmentService.assignPersonalizedQuestions(userId, sessionType, totalQuestions);
        
        log.info("문제 할당 완료: types={}, questionsPerType={}", 
            assignment.getSelectedQuestionTypes(), assignment.getQuestionsPerType());
        
        // 2. 학습 세션 생성 (할당된 문제 정보 포함)
        LearningSessionDto.SessionResponse session = learningSessionService.startLearningSession(
            userId, sessionType, assignment);
        
        log.info("개인화된 학습 세션 시작 완료: sessionId={}, userId={}, assignedQuestions={}", 
            session.getSessionId(), userId, assignment.getAssignedQuestionIds().size());
        
        return session;
    }

    /**
     * 문제 답변 결과 수신 (이벤트 발행 없음)
     */
    public void processQuestionAnswer(String sessionId, String questionId, boolean isCorrect, 
                                   Integer timeSpent, String userNotes) {
        log.info("문제 답변 처리: sessionId={}, questionId={}, isCorrect={}", 
            sessionId, questionId, isCorrect);
        
        // 1. 세션 상태 업데이트 (답변 데이터는 DB에 저장만)
        learningSessionService.receiveQuestionAnswer(sessionId, questionId, isCorrect, timeSpent, userNotes);
        
        log.info("문제 답변 처리 완료: sessionId={}, questionId={}", sessionId, questionId);
    }

    /**
     * 학습 세션 완료 및 이벤트 발행
     */
    public LearningSessionDto.SessionResponse completeLearningSession(String sessionId) {
        log.info("학습 세션 완료 오케스트레이션: sessionId={}", sessionId);
        
        // 1. 학습 세션 완료 처리
        LearningSessionDto.SessionResponse completedSession = learningSessionService.completeLearningSession(sessionId);
        
        // 2. 학습 세션 완료 이벤트 발행 (엔티티 조회 후 이벤트 발행)
        LearningSession session = learningSessionService.getLearningSessionEntity(sessionId);
        eventPublishingService.publishLearningSessionCompletedEvent(session);
        
        log.info("학습 세션 완료 처리 완료: sessionId={}, score={}", sessionId, completedSession.getScore());
        return completedSession;
    }

    /**
     * 학습 세션 조회
     */
    public LearningSessionDto.SessionResponse getLearningSession(String sessionId) {
        return learningSessionService.getLearningSession(sessionId);
    }

    /**
     * 학습 세션 통계 조회
     */
    public LearningSessionService.SessionStatistics getSessionStatistics(String sessionId) {
        return learningSessionService.getSessionStatistics(sessionId);
    }

    /**
     * 현재 진행 중인 세션 조회
     */
    public LearningSessionDto.SessionResponse getCurrentSession(String userId) {
        return learningSessionService.getCurrentSession(userId);
    }

    /**
     * 학습 세션 재개
     */
    public LearningSessionDto.SessionResponse resumeLearningSession(String sessionId) {
        return learningSessionService.resumeLearningSession(sessionId);
    }

    /**
     * 학습 세션 일시정지
     */
    public LearningSessionDto.SessionResponse pauseLearningSession(String sessionId) {
        return learningSessionService.pauseLearningSession(sessionId);
    }

    /**
     * 학습 세션 중단
     */
    public LearningSessionDto.SessionResponse abandonLearningSession(String sessionId) {
        return learningSessionService.abandonLearningSession(sessionId);
    }

    /**
     * 사용자 학습 세션 목록 조회
     */
    public List<LearningSessionDto.SessionListResponse> getUserLearningSessions(String userId, String status, Integer limit) {
        return learningSessionService.getUserLearningSessions(userId, status, limit);
    }
}
