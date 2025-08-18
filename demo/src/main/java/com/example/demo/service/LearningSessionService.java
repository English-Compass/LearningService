package com.example.demo.service;


import com.example.demo.dto.LearningSessionDto;
import com.example.demo.entity.LearningSession;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.repository.LearningSessionRepository;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.service.PersonalizedQuestionAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningSessionService {

    private final LearningSessionRepository sessionRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    // TODO: Redis 설정 시 활성화
    // private final RedisCacheService redisCacheService;

    // ===== 학습 세션 관리 메서드들 =====

    /**
     * 개인화된 학습 세션 시작
     */
    @Transactional
    public LearningSessionDto.SessionResponse startLearningSession(String userId, String sessionType, 
                                             PersonalizedQuestionAssignmentService.QuestionAssignmentResult assignment) {
        log.info("개인화된 학습 세션 시작: userId={}, sessionType={}, selectedQuestionTypes={}", 
            userId, sessionType, assignment.getSelectedQuestionTypes());
        
        String sessionId = UUID.randomUUID().toString();
        LearningSession session = LearningSession.builder()
            .sessionId(sessionId)
            .userId(userId)
            .sessionType(LearningSession.SessionType.valueOf(sessionType)) // String을 enum으로 변환
            .startedAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .status(LearningSession.SessionStatus.STARTED)
            .totalQuestions(assignment.getAssignedQuestionIds().size())
            .answeredQuestions(0)
            .correctAnswers(0)
            .wrongAnswers(0)
            .build();
        
        LearningSession savedSession = sessionRepository.save(session);
        
        // TODO: Redis 설정 시 활성화
        // 캐시에 세션 정보 저장
        // redisCacheService.cacheSession(sessionId, savedSession);
        
        // 할당된 문제 정보도 캐시에 저장
        // redisCacheService.cacheSessionQuestions(sessionId, assignment.getAssignedQuestionIds());
        
        // DTO로 변환하여 반환
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(savedSession.getSessionId())
            .userId(savedSession.getUserId())
            .sessionType(savedSession.getSessionType().name())
            .status(savedSession.getStatus().name())
            .totalQuestions(savedSession.getTotalQuestions())
            .answeredQuestions(savedSession.getAnsweredQuestions())
            .correctAnswers(savedSession.getCorrectAnswers())
            .wrongAnswers(savedSession.getWrongAnswers())
            .score(savedSession.getScore())
            .progressPercentage(savedSession.getProgressPercentage())
            .startedAt(savedSession.getStartedAt())
            .lastUpdatedAt(savedSession.getLastUpdatedAt())
            .build();
    }



    /**
     * 학습 세션 조회 (내부용 - 엔티티 반환)
     */
    public LearningSession getLearningSessionEntity(String sessionId) {

        // TODO: Redis 설정 시 활성화
        // 캐시에서 우선 조회
        // Optional<LearningSession> cachedSession = redisCacheService.getSession(sessionId, LearningSession.class);
        
        // if (cachedSession.isPresent()) {
        //     log.debug("캐시에서 세션 정보 조회: sessionId={}", sessionId);
        //     return cachedSession.get();
        // }
        
        LearningSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        // TODO: Redis 설정 시 활성화
        // redisCacheService.cacheSession(sessionId, session);
        return session;
    }
    
    /**
     * 학습 세션 조회 (외부용 - DTO 반환)
     */
    public LearningSessionDto.SessionResponse getLearningSession(String sessionId) {
        LearningSession session = getLearningSessionEntity(sessionId);
        
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .sessionType(session.getSessionType().name())
            .status(session.getStatus().name())
            .totalQuestions(session.getTotalQuestions())
            .answeredQuestions(session.getAnsweredQuestions())
            .correctAnswers(session.getCorrectAnswers())
            .wrongAnswers(session.getWrongAnswers())
            .score(session.getScore())
            .progressPercentage(session.getProgressPercentage())
            .startedAt(session.getStartedAt())
            .lastUpdatedAt(session.getLastUpdatedAt())
            .build();
    }

    /**
     * 사용자의 학습 세션 목록 조회
     */
    public List<LearningSessionDto.SessionListResponse> getUserLearningSessions(String userId, String status, Integer limit) {
        log.info("사용자 학습 세션 목록 조회: userId={}, status={}, limit={}", userId, status, limit);
        
        List<LearningSession> sessions;
        
        if (status != null && !status.isEmpty()) {
            try {
                LearningSession.SessionStatus sessionStatus = LearningSession.SessionStatus.valueOf(status.toUpperCase());
                sessions = sessionRepository.findByUserIdAndStatusOrderByStartedAtDesc(userId, sessionStatus);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 세션 상태: {}", status);
                sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
            }
        } else {
            sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        }
        
        if (limit != null && limit > 0) {
            sessions = sessions.stream().limit(limit).toList();
        }
        
        // 사용자별 세션 목록 캐시 저장
        // redisCacheService.cacheUserSessions(userId, sessions.stream().map(Object.class::cast).toList());
        
        // DTO로 변환하여 반환
        return sessions.stream()
            .map(session -> LearningSessionDto.SessionListResponse.builder()
                .sessionId(session.getSessionId())
                .sessionType(session.getSessionType().name())
                .startedAt(session.getStartedAt())
                .completedAt(session.getCompletedAt())
                .totalQuestions(session.getTotalQuestions())
                .answeredQuestions(session.getAnsweredQuestions())
                .score(session.getScore())
                .status(session.getStatus())
                .progressPercentage(session.getProgressPercentage())
                .accuracyPercentage(session.getAccuracyPercentage())
                .build())
            .toList();
    }

    /**
     * 문제 답변 결과 수신 및 세션 업데이트
     */
    @Transactional
    public void receiveQuestionAnswer(String sessionId, String questionId, boolean isCorrect, 
                                   Integer timeSpent, String userNotes) {
        log.info("문제 답변 결과 수신: sessionId={}, questionId={}, isCorrect={}", 
            sessionId, questionId, isCorrect);
        
        // 1. 세션 조회
        LearningSession session = getLearningSessionEntity(sessionId);
        
        // 2. 세션 상태 업데이트
        session.updateQuestionAnswer(isCorrect);
        LearningSession updatedSession = sessionRepository.save(session);
        
        // 3. 캐시 업데이트
        updateSessionCache(updatedSession);
    }

    /**
     * 학습 세션 완료
     */
    @Transactional
    public LearningSessionDto.SessionResponse completeLearningSession(String sessionId) {
        log.info("학습 세션 완료: sessionId={}", sessionId);
        
        LearningSession session = getLearningSessionEntity(sessionId);
        session.completeSession();
        LearningSession completedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(completedSession);
        
        // DTO로 변환하여 반환
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(completedSession.getSessionId())
            .userId(completedSession.getUserId())
            .sessionType(completedSession.getLearningItemId())
            .status(completedSession.getStatus().name())
            .totalQuestions(completedSession.getTotalQuestions())
            .answeredQuestions(completedSession.getAnsweredQuestions())
            .correctAnswers(completedSession.getCorrectAnswers())
            .wrongAnswers(completedSession.getWrongAnswers())
            .score(completedSession.getScore())
            .progressPercentage(completedSession.getProgressPercentage())
            .startedAt(completedSession.getStartedAt())
            .lastUpdatedAt(completedSession.getLastUpdatedAt())
            .build();
    }

    /**
     * 학습 세션 중단
     */
    @Transactional
    public LearningSessionDto.SessionResponse abandonLearningSession(String sessionId) {
        log.info("학습 세션 중단: sessionId={}", sessionId);
        
        LearningSession session = getLearningSessionEntity(sessionId);
        session.abandonSession();
        LearningSession abandonedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(abandonedSession);
        
        // DTO로 변환하여 반환
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(abandonedSession.getSessionId())
            .userId(abandonedSession.getUserId())
            .sessionType(abandonedSession.getLearningItemId())
            .status(abandonedSession.getStatus().name())
            .totalQuestions(abandonedSession.getTotalQuestions())
            .answeredQuestions(abandonedSession.getAnsweredQuestions())
            .correctAnswers(abandonedSession.getCorrectAnswers())
            .wrongAnswers(abandonedSession.getWrongAnswers())
            .score(abandonedSession.getScore())
            .progressPercentage(abandonedSession.getProgressPercentage())
            .startedAt(abandonedSession.getStartedAt())
            .lastUpdatedAt(abandonedSession.getLastUpdatedAt())
            .build();
    }

    /**
     * 학습 세션 통계 조회
     */
    public SessionStatistics getSessionStatistics(String sessionId) {
        LearningSessionDto.SessionResponse sessionResponse = getLearningSession(sessionId);
        
        // DTO에서 필요한 정보 추출
        String sessionIdFromResponse = sessionResponse.getSessionId();
        Integer totalQuestions = sessionResponse.getTotalQuestions();
        Integer answeredQuestions = sessionResponse.getAnsweredQuestions();
        Integer correctAnswers = sessionResponse.getCorrectAnswers();
        Integer wrongAnswers = sessionResponse.getWrongAnswers();
        Integer score = sessionResponse.getScore();
        
        // 답변 히스토리 조회
        List<QuestionAnswer> answers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(sessionId);
        
        // 오답 분석 데이터 생성
        List<String> wrongQuestions = answers.stream()
            .filter(answer -> !answer.getIsCorrect())
            .map(QuestionAnswer::getQuestionId)
            .toList();
        

        
        return SessionStatistics.builder()
            .sessionId(sessionId)
            .totalQuestions(totalQuestions)
            .answeredQuestions(answeredQuestions)
            .correctAnswers(correctAnswers)
            .wrongAnswers(wrongAnswers)
            .score(0) // TODO: 점수 계산 로직 필요
            .wrongQuestions(wrongQuestions)
            .build();
    }

    /**
     * 학습 세션 일시정지
     */
    @Transactional
    public LearningSessionDto.SessionResponse pauseLearningSession(String sessionId) {
        log.info("학습 세션 일시정지: sessionId={}", sessionId);
        
        LearningSession session = getLearningSessionEntity(sessionId);
        session.setStatus(LearningSession.SessionStatus.PAUSED);
        session.setLastUpdatedAt(LocalDateTime.now());
        
        LearningSession pausedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(pausedSession);
        
        // DTO로 변환하여 반환
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(pausedSession.getSessionId())
            .userId(pausedSession.getUserId())
            .sessionType(pausedSession.getLearningItemId())
            .status(pausedSession.getStatus().name())
            .totalQuestions(pausedSession.getTotalQuestions())
            .answeredQuestions(pausedSession.getAnsweredQuestions())
            .correctAnswers(pausedSession.getCorrectAnswers())
            .wrongAnswers(pausedSession.getWrongAnswers())
            .score(pausedSession.getScore())
            .progressPercentage(pausedSession.getProgressPercentage())
            .startedAt(pausedSession.getStartedAt())
            .lastUpdatedAt(pausedSession.getLastUpdatedAt())
            .build();
    }

    /**
     * 현재 진행 중인 세션 조회 (내부용 - 엔티티 반환)
     */
    private LearningSession getCurrentSessionEntity(String userId) {
        log.info("현재 진행 중인 세션 조회: userId={}", userId);
        
        // 캐시에서 우선 조회
        // Optional<LearningSession> cachedCurrentSession = redisCacheService.getCurrentSession(userId, LearningSession.class);
        
        // if (cachedCurrentSession.isPresent()) {
        //     log.debug("캐시에서 현재 세션 조회: userId={}", userId);
        //     return cachedCurrentSession.get();
        // }
        
        // DB에서 조회
        LearningSession currentSession = sessionRepository.findByUserIdAndStatusInOrderByStartedAtDesc(
            userId, 
            List.of(LearningSession.SessionStatus.STARTED, LearningSession.SessionStatus.IN_PROGRESS, LearningSession.SessionStatus.PAUSED)
        ).stream().findFirst().orElse(null);
        
        // TODO: Redis 설정 시 활성화
        // if (currentSession != null) {
        //     // 현재 세션 정보 캐시 저장
        //     redisCacheService.cacheCurrentSession(userId, currentSession);
        // }
        
        return currentSession;
    }
    
    /**
     * 현재 진행 중인 세션 조회 (외부용 - DTO 반환)
     */
    public LearningSessionDto.SessionResponse getCurrentSession(String userId) {
        LearningSession currentSession = getCurrentSessionEntity(userId);
        
        if (currentSession == null) {
            return null;
        }
        
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(currentSession.getSessionId())
            .userId(currentSession.getUserId())
            .sessionType(currentSession.getLearningItemId())
            .status(currentSession.getStatus().name())
            .totalQuestions(currentSession.getTotalQuestions())
            .answeredQuestions(currentSession.getAnsweredQuestions())
            .correctAnswers(currentSession.getCorrectAnswers())
            .wrongAnswers(currentSession.getWrongAnswers())
            .score(currentSession.getScore())
            .progressPercentage(currentSession.getProgressPercentage())
            .startedAt(currentSession.getStartedAt())
            .lastUpdatedAt(currentSession.getLastUpdatedAt())
            .build();
    }

    /**
     * 학습 세션 재개
     */
    @Transactional
    public LearningSessionDto.SessionResponse resumeLearningSession(String sessionId) {
        log.info("세션 재개 시작: sessionId={}", sessionId);
        
        // 캐시에서 우선 조회
        // Optional<LearningSession> cachedSession = redisCacheService.getSession(sessionId, LearningSession.class);
        LearningSession session;
        
        // if (cachedSession.isPresent()) {
        //     log.debug("캐시에서 세션 정보 조회: sessionId={}", sessionId);
        //     session = cachedSession.get();
        // } else {
            // DB에서 조회 후 캐시 저장
            session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
            
            // TODO: Redis 설정 시 활성화
            // 세션 정보 캐시 저장
            // redisCacheService.cacheSession(sessionId, session);
        // }
        
        // 세션 상태를 진행 중으로 변경
        session.setStatus(LearningSession.SessionStatus.IN_PROGRESS);
        session.setLastUpdatedAt(LocalDateTime.now());
        
        LearningSession updatedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(updatedSession);
        
        log.info("세션 재개 완료: sessionId={}, status={}", sessionId, updatedSession.getStatus());
        
        // DTO로 변환하여 반환
        return LearningSessionDto.SessionResponse.builder()
            .sessionId(updatedSession.getSessionId())
            .userId(updatedSession.getUserId())
            .sessionType(updatedSession.getLearningItemId())
            .status(updatedSession.getStatus().name())
            .totalQuestions(updatedSession.getTotalQuestions())
            .answeredQuestions(updatedSession.getAnsweredQuestions())
            .correctAnswers(updatedSession.getCorrectAnswers())
            .wrongAnswers(updatedSession.getWrongAnswers())
            .score(updatedSession.getScore())
            .progressPercentage(updatedSession.getProgressPercentage())
            .startedAt(updatedSession.getStartedAt())
            .lastUpdatedAt(updatedSession.getLastUpdatedAt())
            .build();
    }

    /**
     * 사용자 접속 시 자동 세션 복구 및 다음 문제 제공
     */
    public SessionResumeInfo autoResumeUserSession(String userId) {
        log.info("사용자 세션 자동 복구 시작: userId={}", userId);
        
        // 1. 현재 진행 중인 세션 조회
        LearningSession currentSession = getCurrentSessionEntity(userId);
        
        if (currentSession == null) {
            log.info("진행 중인 세션이 없음: userId={}", userId);
            return SessionResumeInfo.builder()
                .hasActiveSession(false)
                .message("진행 중인 학습 세션이 없습니다.")
                .build();
        }
        
        // 2. 세션 상태에 따른 처리
        switch (currentSession.getStatus()) {
            case STARTED:
                // 시작된 세션이지만 아직 문제를 풀지 않은 경우
                return handleStartedSession(currentSession);
                
            case IN_PROGRESS:
                // 진행 중인 세션인 경우
                return handleInProgressSession(currentSession);
                
            case PAUSED:
                // 일시정지된 세션인 경우 자동 재개
                return handlePausedSession(currentSession);
                
            default:
                log.warn("예상치 못한 세션 상태: sessionId={}, status={}", 
                    currentSession.getSessionId(), currentSession.getStatus());
                return SessionResumeInfo.builder()
                    .hasActiveSession(false)
                    .message("세션 상태 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 시작된 세션 처리 (첫 문제 제공)
     */
    private SessionResumeInfo handleStartedSession(LearningSession session) {
        log.info("시작된 세션 처리: sessionId={}", session.getSessionId());
        
        // 캐시에서 할당된 문제 목록 조회
        // Optional<List<String>> questionIds = redisCacheService.getSessionQuestions(session.getSessionId());
        
        // if (questionIds.isEmpty()) {
        //     log.warn("세션 문제 목록이 캐시에 없음: sessionId={}", session.getSessionId());
        //     return SessionResumeInfo.builder()
        //         .hasActiveSession(false)
        //         .message("세션 문제 정보를 찾을 수 없습니다.")
        //         .build();
        // }
        
        // String firstQuestionId = questionIds.get().get(0);
        
        return SessionResumeInfo.builder()
            .hasActiveSession(true)
            .sessionId(session.getSessionId())
            .sessionType(session.getLearningItemId())
            .currentQuestionNumber(1)
            .totalQuestions(session.getTotalQuestions())
            .nextQuestionId(null) // 첫 문제는 직접 제공
            .message("학습 세션을 시작합니다. 첫 번째 문제를 풀어보세요.")
            .build();
    }

    /**
     * 진행 중인 세션 처리 (다음 문제 제공)
     */
    private SessionResumeInfo handleInProgressSession(LearningSession session) {
        log.info("진행 중인 세션 처리: sessionId={}", session.getSessionId());
        
        int currentQuestionNumber = session.getAnsweredQuestions() + 1;
        
        if (currentQuestionNumber > session.getTotalQuestions()) {
            log.info("모든 문제를 풀었음: sessionId={}", session.getSessionId());
            return SessionResumeInfo.builder()
                .hasActiveSession(true)
                .sessionId(session.getSessionId())
                .currentQuestionNumber(currentQuestionNumber - 1)
                .totalQuestions(session.getTotalQuestions())
                .message("모든 문제를 풀었습니다. 세션을 완료해주세요.")
                .build();
        }
        
        // 캐시에서 할당된 문제 목록 조회
        // Optional<List<String>> questionIds = redisCacheService.getSessionQuestions(session.getSessionId());
        
        // if (questionIds.isEmpty()) {
        //     log.warn("세션 문제 목록이 캐시에 없음: sessionId={}", session.getSessionId());
        //     return SessionResumeInfo.builder()
        //         .hasActiveSession(false)
        //         .message("세션 문제 정보를 찾을 수 없습니다.")
        //         .build();
        // }
        
        // String nextQuestionId = questionIds.get().get(currentQuestionNumber - 1);
        
        return SessionResumeInfo.builder()
            .hasActiveSession(true)
            .sessionId(session.getSessionId())
            .sessionType(session.getLearningItemId())
            .currentQuestionNumber(currentQuestionNumber)
            .totalQuestions(session.getTotalQuestions())
            .nextQuestionId(null) // 다음 문제는 직접 제공
            .message("학습을 계속합니다. 다음 문제를 풀어보세요.")
            .build();
    }

    /**
     * 일시정지된 세션 처리 (자동 재개)
     */
    private SessionResumeInfo handlePausedSession(LearningSession session) {
        log.info("일시정지된 세션 자동 재개: sessionId={}", session.getSessionId());
        
        // 세션 상태를 IN_PROGRESS로 변경
        LearningSessionDto.SessionResponse resumedSession = resumeLearningSession(session.getSessionId());
        
        // 진행 중인 세션으로 처리 (DTO를 엔티티로 변환)
        LearningSession sessionEntity = getLearningSessionEntity(session.getSessionId());
        return handleInProgressSession(sessionEntity);
    }

    // ===== 유틸리티 메서드들 =====

    /**
     * 세션 캐시 업데이트
     */
    private void updateSessionCache(LearningSession session) {
        // 세션 정보 캐시 저장
        // redisCacheService.cacheSession(session.getSessionId(), session);
        
        // 진행 상황 캐시 저장
        Map<String, Object> progress = Map.of(
            "status", session.getStatus(),
            "answeredQuestions", session.getAnsweredQuestions(),
            "progressPercentage", session.getProgressPercentage(),
            "lastUpdatedAt", session.getLastUpdatedAt()
        );
        // redisCacheService.cacheSessionProgress(session.getSessionId(), progress);
    }





    // ===== DTO 클래스들 =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionStatistics {
        private String sessionId; // 세션 ID
        private Integer totalQuestions; // 총 문제 갯수
        private Integer answeredQuestions; // 답변한 문제 갯수
        private Integer correctAnswers; // 정답 갯수
        private Integer wrongAnswers; // 오답 갯수
        private Integer score; // 점수
        private List<String> wrongQuestions; // 오답 문제 목록
    }



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResumeInfo {
        private boolean hasActiveSession;
        private String sessionId;
        private String sessionType;
        private int currentQuestionNumber;
        private int totalQuestions;
        private String nextQuestionId;
        private String message;
    }
}
