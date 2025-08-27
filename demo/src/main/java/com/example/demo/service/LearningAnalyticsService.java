package com.example.demo.service;

import com.example.demo.dto.analytics.PerformanceCard;
import com.example.demo.dto.analytics.WeeklyTrendData;
import com.example.demo.dto.analytics.DailyActivityData;
import com.example.demo.dto.analytics.QuestionTypeChartData;
import com.example.demo.entity.QuestionAnswer;
import com.example.demo.entity.LearningSession;
import com.example.demo.repository.QuestionAnswerRepository;
import com.example.demo.repository.LearningSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 학습 통계 서비스 (웹 대시보드용)
 * 사용자 웹 대시보드에 표시될 통계 데이터만 제공하는 단순화된 서비스
 * - 성과 카드용 요약 통계
 * - 그래프 차트용 시계열 데이터
 * 복잡한 분석 기능은 LearningPatternAnalysisService에서 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningAnalyticsService {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final LearningSessionRepository learningSessionRepository;

    /**
     * 학습 성과 카드 데이터 (대시보드 상단 요약 카드용)
     * 사용자의 기간별 학습 성과를 요약하여 제공
     */
    public PerformanceCard getPerformanceCard(String userId, LocalDate fromDate, LocalDate toDate) {
        log.info("학습 성과 카드 데이터 조회: userId={}, fromDate={}, toDate={}", userId, fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(23, 59, 59);
        
        // 기간 내 모든 세션 데이터 조회 (전체 기간 통계용)
        List<LearningSession> sessions = learningSessionRepository.findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(
            userId, startDateTime, endDateTime);
        
        // 기간 내 모든 답변 데이터 조회
        // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
        List<QuestionAnswer> answers = new ArrayList<>();
        for (LearningSession session : sessions) {
            List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
            answers.addAll(sessionAnswers);
        }
        
        if (answers.isEmpty()) {
            return createEmptyPerformanceCard(userId, fromDate, toDate);
        }
        
        // 기본 통계 계산
        int totalQuestions = answers.size();
        int correctAnswers = (int) answers.stream().filter(QuestionAnswer::getIsCorrect).count();
        double accuracyRate = (double) correctAnswers / totalQuestions * 100;
        
        // 총 학습 시간 계산 (분 단위)
        int totalStudyTime = answers.stream()
            .filter(answer -> answer.getTimeSpent() != null)
            .mapToInt(QuestionAnswer::getTimeSpent)
            .sum() / 60;
        
        // 평균 세션 시간 계산
        double averageSessionTime = sessions.size() > 0 ? (double) totalStudyTime / sessions.size() : 0.0;
        
        // 총 획득 점수 계산 (정답 수 × 5점)
        double totalScore = correctAnswers * 5.0;
        
        return PerformanceCard.builder()
            .userId(userId)                                          // 사용자 ID
            .periodStart(fromDate)                                   // 조회 기간 시작일
            .periodEnd(toDate)                                       // 조회 기간 종료일
            .totalSessions(sessions.size())                          // 해당 기간 내 완료한 총 세션 수
            .totalQuestions(totalQuestions)                          // 해당 기간 내 풀어본 총 문제 수
            .totalCorrectAnswers(correctAnswers)                     // 해당 기간 내 맞힌 총 문제 수
            .overallAccuracyRate(accuracyRate)                      // 해당 기간 내 전체 정답률 (%)
            .totalStudyTime(totalStudyTime)                         // 해당 기간 내 총 학습 시간 (분 단위)
            .averageSessionTime(averageSessionTime)                 // 세션당 평균 학습 시간 (분 단위)
            .totalScore(totalScore)                                 // 해당 기간 내 총 획득 점수 (정답 수 × 5점)
            .studyDays(calculateStudyDays(answers))                 // 해당 기간 내 실제 학습한 일수
            .build();
    }
    
    /**
     * 주간 학습 추이 그래프 데이터 (라인 차트용)
     * 최근 N주간의 주별 학습 성과 추이를 제공
     */
    public List<WeeklyTrendData> getWeeklyTrend(String userId, Integer weeks) {
        int numberOfWeeks = weeks != null ? weeks : 8; // 기본값: 최근 8주
        log.info("주간 학습 추이 그래프 데이터 조회: userId={}, weeks={}", userId, numberOfWeeks);
        
        List<WeeklyTrendData> trendData = new ArrayList<>();
        LocalDate currentWeekStart = getCurrentWeekStartDate();
        
        for (int i = 0; i < numberOfWeeks; i++) {
            LocalDate weekStart = currentWeekStart.minusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            LocalDateTime startDateTime = weekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);
            
            // 해당 주의 세션 데이터 조회 (주간 통계용)
            List<LearningSession> weekSessions = learningSessionRepository.findWeeklySessionsByUserIdAndDateRange(
                userId, startDateTime, endDateTime);
            
            // 해당 주의 답변 데이터 조회
            // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
            List<QuestionAnswer> weekAnswers = new ArrayList<>();
            for (LearningSession session : weekSessions) {
                List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
                weekAnswers.addAll(sessionAnswers);
            }
            
            // 주간 통계 계산
            int totalQuestions = weekAnswers.size();
            int correctAnswers = (int) weekAnswers.stream().filter(QuestionAnswer::getIsCorrect).count();
            double accuracyRate = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0.0;
            
            int studyTime = weekAnswers.stream()
                .filter(answer -> answer.getTimeSpent() != null)
                .mapToInt(QuestionAnswer::getTimeSpent)
                .sum() / 60; // 분 단위
            
            WeeklyTrendData weekData = WeeklyTrendData.builder()
                .weekStartDate(weekStart)                            // 해당 주의 시작일 (월요일)
                .weekEndDate(weekEnd)                                // 해당 주의 종료일 (일요일)
                .weekLabel(String.format("%d월 %d일주", weekStart.getMonthValue(), weekStart.getDayOfMonth())) // 그래프 X축 레이블용
                .sessionsCompleted(weekSessions.size())              // 해당 주에 완료한 세션 수
                .questionsAnswered(totalQuestions)                   // 해당 주에 풀어본 문제 수
                .correctAnswers(correctAnswers)                      // 해당 주에 맞힌 문제 수
                .accuracyRate(accuracyRate)                         // 해당 주의 정답률 (%) - 라인 차트의 주요 지표
                .studyTimeMinutes(studyTime)                        // 해당 주의 총 학습 시간 (분 단위)
                .averageScore(correctAnswers * 5.0)                 // 해당 주의 획득 점수 (정답 수 × 5점)
                .build();
            
            trendData.add(weekData);
        }
        
        // 과거 -> 현재 순서로 정렬 (그래프 X축 시간 순서)
        Collections.reverse(trendData);
        return trendData;
    }
    
    /**
     * 일별 학습 활동 히트맵 데이터 (캘린더 히트맵용)
     * 지정된 기간의 일별 학습 활동 강도를 제공
     */
    public List<DailyActivityData> getDailyActivity(String userId, LocalDate fromDate, LocalDate toDate) {
        log.info("일별 학습 활동 히트맵 데이터 조회: userId={}, fromDate={}, toDate={}", userId, fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(23, 59, 59);
        
        // 기간 내 모든 답변 데이터 조회
        // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
        List<QuestionAnswer> answers = new ArrayList<>();
        List<LearningSession> sessions = learningSessionRepository.findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(
            userId, startDateTime, endDateTime);
        for (LearningSession session : sessions) {
            List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
            answers.addAll(sessionAnswers);
        }
        
        // 일별로 그룹화
        Map<LocalDate, List<QuestionAnswer>> answersByDate = answers.stream()
            .collect(Collectors.groupingBy(answer -> answer.getAnsweredAt().toLocalDate()));
        
        List<DailyActivityData> activities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 지정된 기간의 모든 날짜를 순회
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            List<QuestionAnswer> dayAnswers = answersByDate.getOrDefault(current, new ArrayList<>());
            
            int questionsAnswered = dayAnswers.size();
            int correctAnswers = (int) dayAnswers.stream().filter(QuestionAnswer::getIsCorrect).count();
            double accuracyRate = questionsAnswered > 0 ? (double) correctAnswers / questionsAnswered * 100 : 0.0;
            
            int studyTime = dayAnswers.stream()
                .filter(answer -> answer.getTimeSpent() != null)
                .mapToInt(QuestionAnswer::getTimeSpent)
                .sum() / 60; // 분 단위
            
            // 활동 강도 계산 (히트맵 색상 강도용)
            // 0: 활동 없음, 1: 낮은 활동, 2: 보통 활동, 3: 높은 활동, 4: 매우 높은 활동
            int activityLevel = calculateActivityLevel(questionsAnswered);
            
            DailyActivityData activity = DailyActivityData.builder()
                .date(current.format(formatter))                     // 날짜 문자열 (YYYY-MM-DD 형식)
                .questionsAnswered(questionsAnswered)                // 해당 날짜에 풀어본 문제 수
                .correctAnswers(correctAnswers)                      // 해당 날짜에 맞힌 문제 수
                .accuracyRate(accuracyRate)                         // 해당 날짜의 정답률 (%)
                .studyTimeMinutes(studyTime)                        // 해당 날짜의 학습 시간 (분 단위)
                .activityLevel(activityLevel)                       // 활동 강도 레벨 (0-4, 히트맵 색상용)
                .hasActivity(questionsAnswered > 0)                 // 학습 활동 여부 (boolean)
                .build();
            
            activities.add(activity);
            current = current.plusDays(1);
        }
        
        return activities;
    }
    
    /**
     * 문제 유형별 성과 차트 데이터 (도넛/바 차트용)
     * 지정된 기간의 문제 유형별 학습 성과를 제공
     */
    public List<QuestionTypeChartData> getQuestionTypeChart(String userId, LocalDate fromDate, LocalDate toDate) {
        log.info("문제 유형별 성과 차트 데이터 조회: userId={}, fromDate={}, toDate={}", userId, fromDate, toDate);
        
        LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDateTime = toDate != null ? toDate.atTime(23, 59, 59) : LocalDateTime.now();
        
        // Repository에서 문제 유형별 집계된 통계 데이터 조회
        // QuestionAnswer에는 userId가 없으므로 sessionId를 통해 조회
        List<QuestionTypePerformance> performances = new ArrayList<>();
        List<LearningSession> sessions = learningSessionRepository.findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(
            userId, startDateTime, endDateTime);
        
        // 문제 유형별로 통계 수집
        Map<String, List<QuestionAnswer>> answersByType = new HashMap<>();
        for (LearningSession session : sessions) {
            List<QuestionAnswer> sessionAnswers = questionAnswerRepository.findBySessionIdOrderByAnsweredAtAsc(session.getSessionId());
            for (QuestionAnswer answer : sessionAnswers) {
                // Question과 JOIN하여 문제 유형 정보 가져오기
                // TODO: Question 엔티티 조회 로직 필요
                String questionType = "UNKNOWN"; // 임시값
                answersByType.computeIfAbsent(questionType, k -> new ArrayList<>()).add(answer);
            }
        }
        
        // 통계 계산
        for (Map.Entry<String, List<QuestionAnswer>> entry : answersByType.entrySet()) {
            String questionType = entry.getKey();
            List<QuestionAnswer> typeAnswers = entry.getValue();
            
            int totalQuestions = typeAnswers.size();
            int correctAnswers = (int) typeAnswers.stream().filter(QuestionAnswer::getIsCorrect).count();
            double accuracyRate = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0.0;
            double averageTimeSpent = typeAnswers.stream()
                .filter(answer -> answer.getTimeSpent() != null)
                .mapToInt(QuestionAnswer::getTimeSpent)
                .average()
                .orElse(0.0);
            
            QuestionTypePerformance performance = QuestionTypePerformance.builder()
                .questionType(questionType)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .accuracyRate(accuracyRate)
                .averageTimeSpent(averageTimeSpent)
                .build();
            
            performances.add(performance);
        }
        
        if (performances.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 전체 답변 수 계산 (통계 데이터의 합계)
        int totalAnswers = performances.stream()
            .mapToInt(QuestionTypePerformance::getTotalQuestions)
            .sum();
        
        List<QuestionTypeChartData> chartData = new ArrayList<>();
        
        for (QuestionTypePerformance performance : performances) {
            // 통계 계산
            int wrongAnswers = performance.getTotalQuestions() - performance.getCorrectAnswers();
            double accuracyRate = performance.getAccuracyRate();
            double percentage = totalAnswers > 0 ? (double) performance.getTotalQuestions() / totalAnswers * 100 : 0.0;
            double score = performance.getCorrectAnswers() * 5.0;
            
            QuestionTypeChartData data = QuestionTypeChartData.builder()
                .questionType(performance.getQuestionType())              // 문제 유형 코드 (FILL_IN_THE_BLANK 등)
                .displayName(getQuestionTypeDisplayName(performance.getQuestionType())) // 사용자에게 표시될 한글명 ("빈칸 채우기" 등)
                .totalQuestions(performance.getTotalQuestions())          // 해당 유형의 총 문제 수
                .correctAnswers(performance.getCorrectAnswers())          // 해당 유형의 정답 수
                .wrongAnswers(wrongAnswers)                              // 해당 유형의 오답 수
                .accuracyRate(accuracyRate)                             // 해당 유형의 정답률 (%)
                .percentage(percentage)                                 // 전체 문제 중 해당 유형이 차지하는 비율 (%)
                .score(score)                                           // 해당 유형에서 획득한 점수 (정답 수 × 5점)
                .performanceLevel(determinePerformanceLevel(accuracyRate)) // 성과 레벨 (EXCELLENT, GOOD, AVERAGE, POOR)
                .build();
            
            chartData.add(data);
        }
        
        // 정답률 내림차순 정렬 (성과가 좋은 유형부터 표시)
        chartData.sort((a, b) -> Double.compare(b.getAccuracyRate(), a.getAccuracyRate()));
        return chartData;
    }

    /**
     * 문제 유형별 성과 정보를 담는 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QuestionTypePerformance {
        private String questionType;
        private int totalQuestions;
        private int correctAnswers;
        private double accuracyRate;
        private double averageTimeSpent;
    }
    
    // ===== 유틸리티 메서드들 =====
    
    /**
     * 빈 성과 카드 생성 (데이터가 없을 때)
     */
    private PerformanceCard createEmptyPerformanceCard(String userId, LocalDate fromDate, LocalDate toDate) {
        return PerformanceCard.builder()
            .userId(userId)
            .periodStart(fromDate)
            .periodEnd(toDate)
            .totalSessions(0)
            .totalQuestions(0)
            .totalCorrectAnswers(0)
            .overallAccuracyRate(0.0)
            .totalStudyTime(0)
            .averageSessionTime(0.0)
            .totalScore(0.0)
            .studyDays(0)
            .build();
    }
    
    /**
     * 실제 학습한 일수 계산
     */
    private int calculateStudyDays(List<QuestionAnswer> answers) {
        return (int) answers.stream()
            .map(answer -> answer.getAnsweredAt().toLocalDate())
            .distinct()
            .count();
    }
    
    /**
     * 현재 주의 월요일 날짜 반환
     */
    private LocalDate getCurrentWeekStartDate() {
        LocalDate today = LocalDate.now();
        return today.with(java.time.DayOfWeek.MONDAY);
    }
    
    /**
     * 일별 활동 강도 레벨 계산 (히트맵 색상 강도용)
     */
    private int calculateActivityLevel(int questionsAnswered) {
        if (questionsAnswered == 0) return 0;          // 활동 없음
        if (questionsAnswered <= 5) return 1;          // 낮은 활동 (1-5문제)
        if (questionsAnswered <= 15) return 2;         // 보통 활동 (6-15문제)
        if (questionsAnswered <= 25) return 3;         // 높은 활동 (16-25문제)
        return 4;                                       // 매우 높은 활동 (26문제 이상)
    }
    
    /**
     * 문제 유형 표시명 반환
     */
    private String getQuestionTypeDisplayName(String questionType) {
        switch (questionType) {
            case "FILL_IN_THE_BLANK": return "빈칸 채우기";
            case "IDIOM_IN_CONTEXT": return "문장 속 특정 숙어";
            case "PHONETIC_SYMBOL_FINDING": return "발음 기호 찾기";
            default: return questionType;
        }
    }

    /**
     * 정답률 기반 성과 레벨 결정
     */
    private String determinePerformanceLevel(double accuracyRate) {
        if (accuracyRate >= 90.0) return "EXCELLENT";      // 우수 (90% 이상)
        if (accuracyRate >= 80.0) return "GOOD";           // 좋음 (80-89%)
        if (accuracyRate >= 70.0) return "AVERAGE";        // 보통 (70-79%)
        return "POOR";                                      // 부족 (70% 미만)
    }
}
