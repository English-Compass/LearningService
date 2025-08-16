package com.example.demo.service;

import com.example.demo.dto.LearningSessionCompletedEvent;
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
    private final RedisCacheService redisCacheService;

    // ===== 학습 세션 관리 메서드들 =====

    /**
     * 개인화된 학습 세션 시작
     */
    @Transactional
    public LearningSession startLearningSession(String userId, String sessionType, 
                                             PersonalizedQuestionAssignmentService.QuestionAssignmentResult assignment) {
        log.info("개인화된 학습 세션 시작: userId={}, sessionType={}, selectedQuestionTypes={}", 
            userId, sessionType, assignment.getSelectedQuestionTypes());
        
        String sessionId = UUID.randomUUID().toString();
        LearningSession session = LearningSession.builder()
            .sessionId(sessionId)
            .userId(userId)
            .learningItemId(assignment.getSessionType()) // sessionType 사용
            .sessionType(LearningSession.SessionType.PRACTICE)
            .startedAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .status(LearningSession.SessionStatus.STARTED)
            .totalQuestions(assignment.getAssignedQuestionIds().size())
            .answeredQuestions(0)
            .correctAnswers(0)
            .wrongAnswers(0)
            .build();
        
        LearningSession savedSession = sessionRepository.save(session);
        
        // 캐시에 세션 정보 저장
        redisCacheService.cacheSession(sessionId, savedSession);
        
        // 할당된 문제 정보도 캐시에 저장
        redisCacheService.cacheSessionQuestions(sessionId, assignment.getAssignedQuestionIds());
        
        return savedSession;
    }



    /**
     * 학습 세션 조회
     */
    public LearningSession getLearningSession(String sessionId) {
        // 캐시에서 우선 조회
        Optional<LearningSession> cachedSession = redisCacheService.getSession(sessionId, LearningSession.class);
        
        if (cachedSession.isPresent()) {
            log.debug("캐시에서 세션 정보 조회: sessionId={}", sessionId);
            return cachedSession.get();
        }
        
        LearningSession session = sessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
        
        redisCacheService.cacheSession(sessionId, session);
        return session;
    }

    /**
     * 사용자의 학습 세션 목록 조회
     */
    public List<LearningSession> getUserLearningSessions(String userId, String status, Integer limit) {
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
        redisCacheService.cacheUserSessions(userId, sessions.stream().map(Object.class::cast).toList());
        
        return sessions;
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
        LearningSession session = getLearningSession(sessionId);
        
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
    public LearningSessionCompletedEvent completeLearningSession(String sessionId) {
        log.info("학습 세션 완료: sessionId={}", sessionId);
        
        LearningSession session = getLearningSession(sessionId);
        session.completeSession();
        LearningSession completedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(completedSession);
        
        return LearningSessionCompletedEvent.builder()
            .sessionId(sessionId)
            .userId(completedSession.getUserId())
            .learningItemId(completedSession.getLearningItemId())
            .totalQuestions(completedSession.getTotalQuestions())
            .answeredQuestions(completedSession.getAnsweredQuestions())
            .correctAnswers(completedSession.getCorrectAnswers())
            .wrongAnswers(completedSession.getWrongAnswers())
            .score(completedSession.getScore())
            .startedAt(completedSession.getStartedAt())
            .completedAt(completedSession.getCompletedAt())
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * 학습 세션 중단
     */
    @Transactional
    public LearningSession abandonLearningSession(String sessionId) {
        log.info("학습 세션 중단: sessionId={}", sessionId);
        
        LearningSession session = getLearningSession(sessionId);
        session.abandonSession();
        LearningSession abandonedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(abandonedSession);
        
        return abandonedSession;
    }

    /**
     * 학습 세션 통계 조회
     */
    public SessionStatistics getSessionStatistics(String sessionId) {
        LearningSession session = getLearningSession(sessionId);
        
        // 답변 히스토리 조회
        List<QuestionAnswer> answers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(sessionId);
        
        // 오답 분석 데이터 생성
        List<String> wrongQuestions = answers.stream()
            .filter(answer -> !answer.getIsCorrect())
            .map(QuestionAnswer::getQuestionId)
            .toList();
        
        // 카테고리별 성과 분석
        Map<String, Integer> categoryPerformance = analyzeCategoryPerformance(answers);
        
        // 상세한 문제 답변 데이터 생성
        Map<String, QuestionDetail> detailedAnswers = createDetailedAnswers(answers);
        
        // 문제 유형별 요약 생성
        Map<String, QuestionTypeSummary> questionTypeSummary = createQuestionTypeSummary(answers);
        
        return SessionStatistics.builder()
            .sessionId(sessionId)
            .totalQuestions(session.getTotalQuestions())
            .answeredQuestions(session.getAnsweredQuestions())
            .correctAnswers(session.getCorrectAnswers())
            .wrongAnswers(session.getWrongAnswers())
            .score(session.getScore())
            .wrongQuestions(wrongQuestions)
            .categoryPerformance(categoryPerformance)
            .detailedAnswers(detailedAnswers)
            .questionTypeSummary(questionTypeSummary)
            .build();
    }

    /**
     * 학습 세션 일시정지
     */
    @Transactional
    public LearningSession pauseLearningSession(String sessionId) {
        log.info("학습 세션 일시정지: sessionId={}", sessionId);
        
        LearningSession session = getLearningSession(sessionId);
        session.setStatus(LearningSession.SessionStatus.PAUSED);
        session.setLastUpdatedAt(LocalDateTime.now());
        
        LearningSession pausedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(pausedSession);
        
        return pausedSession;
    }

    /**
     * 현재 진행 중인 세션 조회
     */
    public LearningSession getCurrentSession(String userId) {
        log.info("현재 진행 중인 세션 조회: userId={}", userId);
        
        // 캐시에서 우선 조회
        Optional<LearningSession> cachedCurrentSession = redisCacheService.getCurrentSession(userId, LearningSession.class);
        
        if (cachedCurrentSession.isPresent()) {
            log.debug("캐시에서 현재 세션 조회: userId={}", userId);
            return cachedCurrentSession.get();
        }
        
        // DB에서 조회
        LearningSession currentSession = sessionRepository.findByUserIdAndStatusInOrderByStartedAtDesc(
            userId, 
            List.of(LearningSession.SessionStatus.STARTED, LearningSession.SessionStatus.IN_PROGRESS, LearningSession.SessionStatus.PAUSED)
        ).stream().findFirst().orElse(null);
        
        if (currentSession != null) {
            // 현재 세션 정보 캐시 저장
            redisCacheService.cacheCurrentSession(userId, currentSession);
        }
        
        return currentSession;
    }

    /**
     * 학습 세션 재개
     */
    @Transactional
    public LearningSession resumeLearningSession(String sessionId) {
        log.info("세션 재개 시작: sessionId={}", sessionId);
        
        // 캐시에서 우선 조회
        Optional<LearningSession> cachedSession = redisCacheService.getSession(sessionId, LearningSession.class);
        LearningSession session;
        
        if (cachedSession.isPresent()) {
            log.debug("캐시에서 세션 정보 조회: sessionId={}", sessionId);
            session = cachedSession.get();
        } else {
            // DB에서 조회 후 캐시 저장
            session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId));
            
            // 세션 정보 캐시 저장
            redisCacheService.cacheSession(sessionId, session);
        }
        
        // 세션 상태를 진행 중으로 변경
        session.setStatus(LearningSession.SessionStatus.IN_PROGRESS);
        session.setLastUpdatedAt(LocalDateTime.now());
        
        LearningSession updatedSession = sessionRepository.save(session);
        
        // 캐시 업데이트
        updateSessionCache(updatedSession);
        
        log.info("세션 재개 완료: sessionId={}, status={}", sessionId, updatedSession.getStatus());
        
        return updatedSession;
    }

    /**
     * 사용자 접속 시 자동 세션 복구 및 다음 문제 제공
     */
    public SessionResumeInfo autoResumeUserSession(String userId) {
        log.info("사용자 세션 자동 복구 시작: userId={}", userId);
        
        // 1. 현재 진행 중인 세션 조회
        LearningSession currentSession = getCurrentSession(userId);
        
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
        Optional<List<String>> questionIds = redisCacheService.getSessionQuestions(session.getSessionId());
        
        if (questionIds.isEmpty()) {
            log.warn("세션 문제 목록이 캐시에 없음: sessionId={}", session.getSessionId());
            return SessionResumeInfo.builder()
                .hasActiveSession(false)
                .message("세션 문제 정보를 찾을 수 없습니다.")
                .build();
        }
        
        String firstQuestionId = questionIds.get().get(0);
        
        return SessionResumeInfo.builder()
            .hasActiveSession(true)
            .sessionId(session.getSessionId())
            .sessionType(session.getLearningItemId())
            .currentQuestionNumber(1)
            .totalQuestions(session.getTotalQuestions())
            .nextQuestionId(firstQuestionId)
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
        Optional<List<String>> questionIds = redisCacheService.getSessionQuestions(session.getSessionId());
        
        if (questionIds.isEmpty()) {
            log.warn("세션 문제 목록이 캐시에 없음: sessionId={}", session.getSessionId());
            return SessionResumeInfo.builder()
                .hasActiveSession(false)
                .message("세션 문제 정보를 찾을 수 없습니다.")
                .build();
        }
        
        String nextQuestionId = questionIds.get().get(currentQuestionNumber - 1);
        
        return SessionResumeInfo.builder()
            .hasActiveSession(true)
            .sessionId(session.getSessionId())
            .sessionType(session.getLearningItemId())
            .currentQuestionNumber(currentQuestionNumber)
            .totalQuestions(session.getTotalQuestions())
            .nextQuestionId(nextQuestionId)
            .message("학습을 계속합니다. 다음 문제를 풀어보세요.")
            .build();
    }

    /**
     * 일시정지된 세션 처리 (자동 재개)
     */
    private SessionResumeInfo handlePausedSession(LearningSession session) {
        log.info("일시정지된 세션 자동 재개: sessionId={}", session.getSessionId());
        
        // 세션 상태를 IN_PROGRESS로 변경
        LearningSession resumedSession = resumeLearningSession(session.getSessionId());
        
        // 진행 중인 세션으로 처리
        return handleInProgressSession(resumedSession);
    }

    // ===== 유틸리티 메서드들 =====

    /**
     * 세션 캐시 업데이트
     */
    private void updateSessionCache(LearningSession session) {
        // 세션 정보 캐시 저장
        redisCacheService.cacheSession(session.getSessionId(), session);
        
        // 진행 상황 캐시 저장
        Map<String, Object> progress = Map.of(
            "status", session.getStatus(),
            "answeredQuestions", session.getAnsweredQuestions(),
            "progressPercentage", session.getProgressPercentage(),
            "lastUpdatedAt", session.getLastUpdatedAt()
        );
        redisCacheService.cacheSessionProgress(session.getSessionId(), progress);
    }

    /**
     * 카테고리별 성과 분석
     */
    private Map<String, Integer> analyzeCategoryPerformance(List<QuestionAnswer> answers) {
        // TODO: 실제 카테고리별 성과 분석 로직 구현
        // - 대분류별 정답률
        // - 소분류별 정답률
        return Map.of("STUDY", 80, "BUSINESS", 70);
    }

    /**
     * 상세한 문제 답변 데이터 생성
     */
    private Map<String, QuestionDetail> createDetailedAnswers(List<QuestionAnswer> answers) {
        Map<String, QuestionDetail> detailedAnswers = new HashMap<>();
        for (QuestionAnswer answer : answers) {
            QuestionDetail detail = QuestionDetail.builder()
                .questionId(answer.getQuestionId())
                .questionType(answer.getQuestionType())
                .questionText(answer.getQuestionText())
                .options(answer.getOptions())
                .correctAnswer(answer.getCorrectAnswer())
                .userAnswer(answer.getUserAnswer())
                .isCorrect(answer.getIsCorrect())
                .timeSpent(answer.getTimeSpent())
                .userNotes(answer.getUserNotes())
                .answeredAt(answer.getAnsweredAt())
                .build();
            detailedAnswers.put(answer.getQuestionId(), detail);
        }
        return detailedAnswers;
    }

    /**
     * 문제 유형별 요약 생성
     */
    private Map<String, QuestionTypeSummary> createQuestionTypeSummary(List<QuestionAnswer> answers) {
        Map<String, QuestionTypeSummary> summary = new HashMap<>();
        Map<String, List<QuestionAnswer>> groupedAnswers = answers.stream()
            .collect(Collectors.groupingBy(QuestionAnswer::getQuestionType));

        for (Map.Entry<String, List<QuestionAnswer>> entry : groupedAnswers.entrySet()) {
            String questionType = entry.getKey();
            List<QuestionAnswer> typeAnswers = entry.getValue();

            QuestionTypeSummary typeSummary = QuestionTypeSummary.builder()
                .total(typeAnswers.size())
                .correct(typeAnswers.stream().filter(QuestionAnswer::getIsCorrect).count())
                .accuracy(typeAnswers.size() > 0 ? (typeAnswers.stream().filter(QuestionAnswer::getIsCorrect).count() / (double) typeAnswers.size()) * 100 : 0.0)
                .averageTime(typeAnswers.size() > 0 ? typeAnswers.stream().mapToInt(QuestionAnswer::getTimeSpent).average().orElse(0) : 0.0)
                .wrongQuestionIds(typeAnswers.stream()
                    .filter(answer -> !answer.getIsCorrect())
                    .map(QuestionAnswer::getQuestionId)
                    .toList())
                .build();
            summary.put(questionType, typeSummary);
        }
        return summary;
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
        private Map<String, Integer> categoryPerformance; // 카테고리별 성과
        
        // 상세한 문제 답변 데이터 (JSON 형식)
        private Map<String, QuestionDetail> detailedAnswers; // 문제별 상세 답변
        private Map<String, QuestionTypeSummary> questionTypeSummary; // 문제 유형별 요약
    }

    /**
     * 문제별 상세 답변 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDetail {
        private String questionId;           // 문제 ID
        private String questionType;         // 문제 유형 (FILL_IN_THE_BLANK, SYNONYM_SELECTION, PRONUNCIATION_RECOGNITION)
        private String questionText;         // 문제 텍스트
        private Map<String, String> options; // 보기들 (option1, option2, option3, option4)
        private String correctAnswer;        // 정답
        private String userAnswer;           // 사용자 답변
        private boolean isCorrect;           // 정답 여부
        private Integer timeSpent;           // 소요 시간 (초)
        private String userNotes;            // 사용자 노트
        private LocalDateTime answeredAt;    // 답변 시간
    }

    /**
     * 문제 유형별 요약 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypeSummary {
        private Integer total;           // 총 문제 수
        private Integer correct;         // 정답 수
        private Double accuracy;         // 정답률
        private Double averageTime;      // 평균 소요 시간
        private List<String> wrongQuestionIds; // 오답 문제 ID 목록
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
