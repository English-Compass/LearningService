package com.example.demo.controller;

import com.example.demo.dto.analytics.*;
import com.example.demo.entity.LearningSession;
import com.example.demo.entity.LearningSessionEvent;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.repository.LearningSessionRepository;
import com.example.demo.repository.LearningSessionEventRepository;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.service.LearningAnalyticsService;
import com.example.demo.service.LearningPatternAnalysisService;
import com.example.demo.service.LearningPatternAnalysisService.LearningSessionResult;
import com.example.demo.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

/**
 * 학습 분석 데이터 조회 전용 컨트롤러
 * - 대시보드 통계 API (성과 카드, 차트 데이터)
 * - 학습 패턴 분석 API (문제 할당을 위한 분석 데이터)
 * - 데이터베이스에서 직접 조회하여 통계 데이터 제공
 * - 세션 생성/수정 기능은 제외
 */
@RestController
@RequestMapping("/api/learning-analytics")
@RequiredArgsConstructor
@Slf4j
public class LearningAnalyticsController {

    private final LearningAnalyticsService learningAnalyticsService;
    private final LearningPatternAnalysisService learningPatternAnalysisService;
    private final LearningSessionRepository learningSessionRepository;
    private final LearningSessionEventRepository learningSessionEventRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final RedisCacheService redisCacheService;

    // ===== 학습 패턴 분석 API =====

    /**
     * 전체 학습 기간에 대한 패턴 분석 결과 조회
     * Problem Service에서 복습/오답 세션 문제 할당 시 사용
     * GET /api/learning-analytics/users/{userId}/learning-pattern
     */
    @GetMapping("/users/{userId}/learning-pattern")
    public ResponseEntity<LearningPatternAnalysisDTO> getCompleteLearningPattern(
            @PathVariable String userId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        
        log.info("전체 학습 패턴 분석 조회: userId={}, fromDate={}, toDate={}", 
            userId, fromDate, toDate);
        
        // 기본값: 최근 30일
        LocalDate startDate = fromDate != null ? fromDate : LocalDate.now().minusDays(29);
        LocalDate endDate = toDate != null ? toDate : LocalDate.now();
        
        // LocalDate를 LocalDateTime으로 변환 (시작일 00:00, 종료일 23:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        try {
            LearningPatternAnalysisDTO analysis = learningPatternAnalysisService.analyzeCompleteLearningIncremental(
                userId, startDateTime, endDateTime);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("학습 패턴 분석 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 개별 세션에 대한 패턴 분석 결과 조회
     * Problem Service에서 특정 세션 기반 문제 할당 시 사용
     * GET /api/learning-analytics/users/{userId}/sessions/{sessionId}/learning-pattern
     */
    @GetMapping("/users/{userId}/sessions/{sessionId}/learning-pattern")
    public ResponseEntity<LearningPatternAnalysisDTO> getSessionLearningPattern(    
            @PathVariable String userId,
            @PathVariable String sessionId) {
        
        log.info("개별 세션 학습 패턴 분석 조회: userId={}, sessionId={}", userId, sessionId);
        
        try {
            // 1. 세션 데이터 조회
            LearningSession session = learningSessionRepository.findById(sessionId)
                .orElse(null);
            
            if (session == null || !session.getUserId().equals(userId)) {
                log.warn("세션을 찾을 수 없거나 접근 권한이 없음: sessionId={}, userId={}", sessionId, userId);
                return ResponseEntity.notFound().build();
            }
            
            // 2. 세션 이벤트 조회
            List<LearningSessionEvent> sessionEvents = learningSessionEventRepository
                .findBySessionId(sessionId)
                .map(List::of)
                .orElse(Collections.emptyList());
            
            // 3. 문제 답변 데이터 조회
            List<QuestionAnswer> questionAnswers = questionAnswerRepository
                .findBySessionIdOrderByAnsweredAtAsc(sessionId);
            
            // 4. LearningSessionResult 생성
            LearningSessionResult sessionResult = LearningSessionResult.builder()
                .userId(userId)
                .sessionId(sessionId)
                .totalQuestions(questionAnswers.size())
                .correctAnswers((int) questionAnswers.stream().filter(QuestionAnswer::getIsCorrect).count())
                .totalDuration(questionAnswers.stream()
                    .filter(qa -> qa.getTimeSpent() != null)
                    .mapToLong(QuestionAnswer::getTimeSpent)
                    .sum())
                .questionAnswers(questionAnswers)
                .build();
            
            // 5. 학습 패턴 분석 수행
            LearningPatternAnalysisDTO analysis = learningPatternAnalysisService
                .performPatternAnalysis(sessionResult);
            
            log.info("세션 학습 패턴 분석 완료: sessionId={}, analysisType={}", sessionId, analysis.getAnalysisType());
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("세션 학습 패턴 분석 중 오류 발생: userId={}, sessionId={}", userId, sessionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 최근 학습 패턴 분석 결과 조회 (캐시된 데이터)
     * 빠른 응답이 필요한 경우 사용
     * GET /api/learning-analytics/users/{userId}/learning-pattern/recent
     */
    @GetMapping("/users/{userId}/learning-pattern/recent")
    public ResponseEntity<LearningPatternAnalysisDTO> getRecentLearningPattern(
            @PathVariable String userId) {
        
        log.info("최근 학습 패턴 분석 결과 조회: userId={}", userId);
        
        try {
            // 1. Redis 캐시에서 최근 분석 결과 조회 시도
            String cacheKey = "learning-pattern:recent:" + userId;
            LearningPatternAnalysisDTO cachedAnalysis = redisCacheService.getTempData(
                "learning-pattern", userId, LearningPatternAnalysisDTO.class).orElse(null);
            
            if (cachedAnalysis != null) {
                log.info("Redis 캐시에서 최근 분석 결과 조회 성공: userId={}", userId);
                return ResponseEntity.ok(cachedAnalysis);
            }
            
            // 2. 캐시에 없으면 최근 30일 데이터로 새로운 분석 수행
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);
            
            LearningPatternAnalysisDTO recentAnalysis = learningPatternAnalysisService
                .analyzeCompleteLearningIncremental(userId, startDate, endDate);
            
            if (recentAnalysis != null) {
                // 3. 분석 결과를 캐시에 저장 (1시간 유효)
                redisCacheService.cacheTempData("learning-pattern", userId, recentAnalysis);
                log.info("최근 학습 패턴 분석 완료 및 캐시 저장: userId={}", userId);
                return ResponseEntity.ok(recentAnalysis);
            } else {
                log.warn("최근 학습 패턴 분석 결과가 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("최근 학습 패턴 분석 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== 대시보드 통계 API =====

    /**
     * 학습 성과 카드 데이터 조회 (대시보드 상단 요약 카드용)
     * GET /api/learning-analytics/users/{userId}/performance-card
     */
    @GetMapping("/users/{userId}/performance-card")
    public ResponseEntity<PerformanceCard> getPerformanceCard(
            @PathVariable String userId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        
        log.info("학습 성과 카드 데이터 조회: userId={}, fromDate={}, toDate={}", 
            userId, fromDate, toDate);
        
        // 기본값: 최근 7일
        LocalDate startDate = fromDate != null ? fromDate : LocalDate.now().minusDays(6);
        LocalDate endDate = toDate != null ? toDate : LocalDate.now();
        
        PerformanceCard card = learningAnalyticsService.getPerformanceCard(userId, startDate, endDate);
        return ResponseEntity.ok(card);
    }

    /**
     * 주간 학습 추이 그래프 데이터 조회 (라인 차트용)
     * GET /api/learning-analytics/users/{userId}/weekly-trend
     */
    @GetMapping("/users/{userId}/weekly-trend")
    public ResponseEntity<List<WeeklyTrendData>> getWeeklyTrend(
            @PathVariable String userId,
            @RequestParam(required = false) Integer weeks) {
        
        log.info("주간 학습 추이 데이터 조회: userId={}, weeks={}", userId, weeks);
        
        List<WeeklyTrendData> trendData = learningAnalyticsService.getWeeklyTrend(userId, weeks);
        return ResponseEntity.ok(trendData);
    }

    /**
     * 일별 학습 활동 히트맵 데이터 조회 (캘린더 히트맵용)
     * GET /api/learning-analytics/users/{userId}/daily-activity
     */
    @GetMapping("/users/{userId}/daily-activity")
    public ResponseEntity<List<DailyActivityData>> getDailyActivity(
            @PathVariable String userId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        
        log.info("일별 학습 활동 히트맵 데이터 조회: userId={}, fromDate={}, toDate={}", 
            userId, fromDate, toDate);
        
        // 기본값: 최근 30일
        LocalDate startDate = fromDate != null ? fromDate : LocalDate.now().minusDays(29);
        LocalDate endDate = toDate != null ? toDate : LocalDate.now();
        
        List<DailyActivityData> activityData = learningAnalyticsService.getDailyActivity(userId, startDate, endDate);
        return ResponseEntity.ok(activityData);
    }

    /**
     * 문제 유형별 성과 차트 데이터 조회 (도넛/바 차트용)
     * GET /api/learning-analytics/users/{userId}/question-type-chart
     */
    @GetMapping("/users/{userId}/question-type-chart")
    public ResponseEntity<List<QuestionTypeChartData>> getQuestionTypeChart(
            @PathVariable String userId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        
        log.info("문제 유형별 차트 데이터 조회: userId={}, fromDate={}, toDate={}", 
            userId, fromDate, toDate);
        
        // 기본값: 최근 30일
        LocalDate startDate = fromDate != null ? fromDate : LocalDate.now().minusDays(29);
        LocalDate endDate = toDate != null ? toDate : LocalDate.now();
        
        List<QuestionTypeChartData> chartData = learningAnalyticsService.getQuestionTypeChart(userId, startDate, endDate);
        return ResponseEntity.ok(chartData);
    }
}