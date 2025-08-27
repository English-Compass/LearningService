package com.example.demo.controller;

import com.example.demo.dto.analytics.PerformanceCard;
import com.example.demo.dto.analytics.WeeklyTrendData;
import com.example.demo.dto.analytics.DailyActivityData;
import com.example.demo.dto.analytics.QuestionTypeChartData;
import com.example.demo.dto.analytics.LearningPatternAnalysisDTO;
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
import java.util.Map;
import java.util.HashMap;

/**
 * 학습 분석 데이터 조회 전용 컨트롤러
 * - 대시보드 통계 API (성과 카드, 차트 데이터)
 * - 학습 패턴 분석 API (문제 할당을 위한 분석 데이터)
 * - 데이터베이스에서 직접 조회하여 통계 데이터 제공
 * - 세션 생성/수정 기능은 제외
 */
@Slf4j
@RestController
@RequestMapping("/api/learning-analytics")
@RequiredArgsConstructor
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
     * 사용자 학습 분석 데이터 조회 (뷰 기반)
     * user_learning_analytics 뷰에서 직접 조회하여 빠른 응답
     * GET /api/learning-analytics/users/{userId}/analytics
     */
    @GetMapping("/users/{userId}/analytics")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable String userId) {
        log.info("사용자 학습 분석 데이터 조회: userId={}", userId);
        
        try {
            Map<String, Object> analytics = learningAnalyticsService.getUserLearningAnalytics(userId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("사용자 학습 분석 데이터 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 카테고리별 성과 데이터 조회 (뷰 기반)
     * category_performance_view 뷰에서 직접 조회
     * GET /api/learning-analytics/users/{userId}/category-performance
     */
    @GetMapping("/users/{userId}/category-performance")
    public ResponseEntity<List<Map<String, Object>>> getCategoryPerformance(@PathVariable String userId) {
        log.info("카테고리별 성과 데이터 조회: userId={}", userId);
        
        try {
            List<Map<String, Object>> categoryPerformance = learningAnalyticsService.getCategoryPerformance(userId);
            return ResponseEntity.ok(categoryPerformance);
        } catch (Exception e) {
            log.error("카테고리별 성과 데이터 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 난이도별 성취도 데이터 조회 (뷰 기반)
     * difficulty_achievement_view 뷰에서 직접 조회
     * GET /api/learning-analytics/users/{userId}/difficulty-achievement
     */
    @GetMapping("/users/{userId}/difficulty-achievement")
    public ResponseEntity<List<Map<String, Object>>> getDifficultyAchievement(@PathVariable String userId) {
        log.info("난이도별 성취도 데이터 조회: userId={}", userId);
        
        try {
            List<Map<String, Object>> difficultyAchievement = learningAnalyticsService.getDifficultyAchievement(userId);
            return ResponseEntity.ok(difficultyAchievement);
        } catch (Exception e) {
            log.error("난이도별 성취도 데이터 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 문제 통계 데이터 조회 (뷰 기반) -- 관리자용 
     * question_stats_view 뷰에서 직접 조회
     * GET /api/learning-analytics/question-stats
     */
    @GetMapping("/question-stats")
    public ResponseEntity<List<Map<String, Object>>> getQuestionStats() {
        log.info("문제 통계 데이터 조회");
        
        try {
            List<Map<String, Object>> questionStats = learningAnalyticsService.getQuestionStats();
            return ResponseEntity.ok(questionStats);
        } catch (Exception e) {
            log.error("문제 통계 데이터 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 저장된 학습 패턴 분석 결과 조회
     * learning_pattern_analysis 테이블에서 조회
     * GET /api/learning-analytics/users/{userId}/stored-pattern/{analysisType} ex)  PERIOD_ANALYSIS, SESSION_ANALYSIS
     */
    @GetMapping("/users/{userId}/stored-pattern/{analysisType}")
    public ResponseEntity<Map<String, Object>> getStoredLearningPattern(
            @PathVariable String userId,
            @PathVariable String analysisType) {
        log.info("저장된 학습 패턴 분석 결과 조회: userId={}, analysisType={}", userId, analysisType);
        
        try {
            Map<String, Object> patternAnalysis = learningAnalyticsService.getStoredLearningPattern(userId, analysisType);
            return ResponseEntity.ok(patternAnalysis);
        } catch (Exception e) {
            log.error("저장된 학습 패턴 분석 결과 조회 중 오류 발생: userId={}, analysisType={}", userId, analysisType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== 기존 API 엔드포인트들 =====

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

    // ===== 학습 시간 통계 API =====

    /**
     * 특정 사용자의 모든 학습 세션에 대한 총 학습 시간 조회
     * GET /api/learning-analytics/users/{userId}/total-learning-time
     */
    @GetMapping("/users/{userId}/total-learning-time")
    public ResponseEntity<Map<String, Object>> getTotalLearningTime(@PathVariable String userId) {
        log.info("사용자 총 학습 시간 조회: userId={}", userId);
        
        try {
            // 기본 학습 시간 정보 조회
            long totalSeconds = learningAnalyticsService.getTotalLearningTimeForUser(userId);
            double totalMinutes = learningAnalyticsService.getTotalLearningTimeMinutesForUser(userId);
            double totalHours = learningAnalyticsService.getTotalLearningTimeHoursForUser(userId);
            
            log.info("기본 학습 시간 조회 완료: totalSeconds={}, totalMinutes={}, totalHours={}", 
                     totalSeconds, totalMinutes, totalHours);
            
            // 세션 타입별 통계 조회
            log.info("세션 타입별 통계 조회 시작");
            List<Map<String, Object>> sessionTypeStats = learningAnalyticsService.getSessionTypeStats(userId);
            log.info("세션 타입별 통계 조회 완료: sessionTypeStats={}", sessionTypeStats);
            
            // 전체 요약 통계 계산
            int totalSessions = sessionTypeStats.stream()
                .mapToInt(stat -> (Integer) stat.get("totalSessions"))
                .sum();
            
            int totalCompletedSessions = sessionTypeStats.stream()
                .mapToInt(stat -> (Integer) stat.get("completedSessions"))
                .sum();
            
            int totalQuestions = sessionTypeStats.stream()
                .mapToInt(stat -> (Integer) stat.get("totalQuestions"))
                .sum();
            
            int totalCorrectAnswers = sessionTypeStats.stream()
                .mapToInt(stat -> (Integer) stat.get("correctAnswers"))
                .sum();
            
            // 전체 정답률 계산
            double overallAccuracyRate = totalQuestions > 0 ? 
                (double) totalCorrectAnswers / totalQuestions * 100 : 0.0;
            
            log.info("통계 계산 완료: totalSessions={}, totalCompletedSessions={}, totalQuestions={}, totalCorrectAnswers={}, overallAccuracyRate={}", 
                     totalSessions, totalCompletedSessions, totalQuestions, totalCorrectAnswers, overallAccuracyRate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalLearningTimeSeconds", totalSeconds);
            response.put("totalLearningTimeMinutes", totalMinutes);
            response.put("totalLearningTimeHours", totalHours);
            response.put("formattedTime", formatLearningTime(totalSeconds));
            response.put("totalSessions", totalSessions);
            response.put("totalCompletedSessions", totalCompletedSessions);
            response.put("totalQuestions", totalQuestions);
            response.put("totalCorrectAnswers", totalCorrectAnswers);
            response.put("overallAccuracyRate", Math.round(overallAccuracyRate * 100.0) / 100.0);
            response.put("sessionTypeStats", sessionTypeStats);
            
            log.info("사용자 총 학습 시간 조회 성공: userId={}, totalSeconds={}, totalSessions={}", 
                     userId, totalSeconds, totalSessions);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 총 학습 시간 조회 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 사용자의 월별 학습 시간 통계 조회
     * GET /api/learning-analytics/users/{userId}/monthly-learning-time
     */
    @GetMapping("/users/{userId}/monthly-learning-time")
    public ResponseEntity<Map<String, Object>> getMonthlyLearningTime(@PathVariable String userId) {
        log.info("사용자 월별 학습 시간 통계 조회: userId={}", userId);
        
        try {
            List<Map<String, Object>> monthlyStats = learningAnalyticsService.getMonthlyLearningTimeStats(userId);
            
            // 전체 요약 통계 계산
            long totalSeconds = monthlyStats.stream()
                .mapToLong(detail -> (Long) detail.get("timeSpentSeconds"))
                .sum();
            
            double totalMinutes = Math.round((double) totalSeconds / 60.0 * 100.0) / 100.0;
            double totalHours = Math.round((double) totalSeconds / 3600.0 * 100.0) / 100.0;
            
            Map<String, Object> response = Map.of(
                "userId", userId,
                "totalMonths", monthlyStats.size(),
                "totalLearningTimeSeconds", totalSeconds,
                "totalLearningTimeMinutes", totalMinutes,
                "totalLearningTimeHours", totalHours,
                "formattedTotalTime", formatLearningTime(totalSeconds),
                "monthlyStats", monthlyStats
            );
            
            log.info("사용자 월별 학습 시간 통계 조회 성공: userId={}, monthCount={}", 
                     userId, monthlyStats.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("사용자 월별 학습 시간 통계 조회 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 학습 시간을 읽기 쉬운 형태로 포맷팅
     */
    private String formatLearningTime(long totalSeconds) {
        if (totalSeconds == 0) {
            return "0시간 0분 0초";
        }
        
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        StringBuilder formatted = new StringBuilder();
        if (hours > 0) {
            formatted.append(hours).append("시간 ");
        }
        if (minutes > 0 || hours > 0) {
            formatted.append(minutes).append("분 ");
        }
        formatted.append(seconds).append("초");
        
        return formatted.toString();
    }
}