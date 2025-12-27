package com.example.demo.repository;

import com.example.demo.entity.QuestionAnswer;
import com.example.demo.entity.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문제 답변 Repository
 * 사용자의 문제 답변 기록을 관리
 * userId가 필요한 경우 LearningSession과 JOIN하여 sessionId를 통해 userId를 가져옴
 */
@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    /**
     * 세션 ID로 답변 기록 조회 (답변 시간 순으로 정렬)
     */
    List<QuestionAnswer> findBySessionIdOrderByAnsweredAtAsc(String sessionId);

    /**
     * 세션 ID와 문제 ID로 답변 기록 조회
     */
    List<QuestionAnswer> findBySessionIdAndQuestionIdOrderByAnsweredAtDesc(String sessionId, String questionId);

    /**
     * 세션 ID와 정답 여부로 답변 기록 조회
     */
    List<QuestionAnswer> findBySessionIdAndIsCorrectOrderByAnsweredAtDesc(String sessionId, Boolean isCorrect);

    /**
     * 세션 ID와 날짜 범위로 답변 기록 조회
     */
    List<QuestionAnswer> findBySessionIdAndAnsweredAtBetween(@Param("sessionId") String sessionId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ===== 고정 주차(월요일~일요일) 기반 메서드들 =====
    // LearningSession과 JOIN하여 sessionId를 통해 userId를 가져옴

    /**
     * 사용자 ID와 주차로 답변 기록 조회 (이번 주)
     * 월요일 00:00:00 ~ 일요일 23:59:59
     */
    @Query("SELECT qa FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startOfWeek AND qa.answeredAt <= :endOfWeek " +
           "ORDER BY qa.answeredAt ASC")
    List<QuestionAnswer> findByUserIdAndThisWeek(@Param("userId") String userId, 
                                                @Param("startOfWeek") LocalDateTime startOfWeek, 
                                                @Param("endOfWeek") LocalDateTime endOfWeek);

    /**
     * 사용자 ID와 주차로 답변 기록 조회 (지정된 주)
     * 해당 주의 월요일 00:00:00 ~ 일요일 23:59:59
     */
    @Query("SELECT qa FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startOfWeek AND qa.answeredAt <= :endOfWeek " +
           "ORDER BY qa.answeredAt ASC")
    List<QuestionAnswer> findByUserIdAndWeekOf(@Param("userId") String userId, 
                                              @Param("startOfWeek") LocalDateTime startOfWeek, 
                                              @Param("endOfWeek") LocalDateTime endOfWeek);

    /**
     * 사용자 ID와 월로 답변 기록 조회 (이번 달)
     * 1일 00:00:00 ~ 말일 23:59:59
     */
    @Query("SELECT qa FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startOfMonth AND qa.answeredAt <= :endOfMonth " +
           "ORDER BY qa.answeredAt ASC")
    List<QuestionAnswer> findByUserIdAndThisMonth(@Param("userId") String userId, 
                                                 @Param("startOfMonth") LocalDateTime startOfMonth, 
                                                 @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 사용자 ID와 월로 답변 기록 조회 (지정된 달)
     * 해당 월의 1일 00:00:00 ~ 말일 23:59:59
     */
    @Query("SELECT qa FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startOfMonth AND qa.answeredAt <= :endOfMonth " +
           "ORDER BY qa.answeredAt ASC")
    List<QuestionAnswer> findByUserIdAndMonthOf(@Param("userId") String userId, 
                                               @Param("startOfMonth") LocalDateTime startOfMonth, 
                                               @Param("endOfMonth") LocalDateTime endOfMonth);

    // ===== 통계 집계 메서드들 =====
    // LearningSession과 JOIN하여 sessionId를 통해 userId를 가져옴

    /**
     * 사용자 ID와 주차로 주간 통계 조회
     */
    @Query("SELECT COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startOfWeek " +
           "AND qa.answeredAt <= :endOfWeek")
    Object[] getWeeklyStatsByUserId(@Param("userId") String userId, 
                                   @Param("startOfWeek") LocalDateTime startOfWeek, 
                                   @Param("endOfWeek") LocalDateTime endOfWeek);

    /**
     * 사용자 ID와 월로 월간 통계 조회
     */
    @Query("SELECT COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startOfMonth " +
           "AND qa.answeredAt <= :endOfMonth")
    Object[] getMonthlyStatsByUserId(@Param("userId") String userId, 
                                    @Param("startOfMonth") LocalDateTime startOfMonth, 
                                    @Param("endOfMonth") LocalDateTime endOfMonth);

    /**
     * 사용자 ID로 최근 N주간 통계 조회
     */
    @Query("SELECT COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent, " +
           "WEEK(qa.answeredAt) as weekNumber " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startDate " +
           "GROUP BY WEEK(qa.answeredAt) " +
           "ORDER BY weekNumber DESC")
    List<Object[]> getRecentWeeksStatsByUserId(@Param("userId") String userId, 
                                              @Param("startDate") LocalDateTime startDate);

    /**
     * 사용자 ID로 최근 N개월간 통계 조회
     */
    @Query("SELECT COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent, " +
           "YEAR(qa.answeredAt) as year, " +
           "MONTH(qa.answeredAt) as month " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startDate " +
           "GROUP BY YEAR(qa.answeredAt), MONTH(qa.answeredAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getRecentMonthsStatsByUserId(@Param("userId") String userId, 
                                               @Param("startDate") LocalDateTime startDate);

    // ===== 문제 유형별 성과 분석 메서드들 =====
    // Question 테이블 없이 question_answer의 questionType 필드 사용

    /**
     * 사용자 ID와 문제 유형으로 답변 기록 조회
     * 문제 유형별 성과 분석에 사용
     */
    @Query("SELECT qa FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId AND qa.questionType = :questionType " +
           "ORDER BY qa.answeredAt ASC")
    List<QuestionAnswer> findByUserIdAndQuestionType(@Param("userId") String userId, 
                                                    @Param("questionType") String questionType);

    /**
     * 세션 ID와 문제 유형으로 답변 기록 조회
     * 특정 세션의 문제 유형별 성과 분석에 사용
     */
    @Query("SELECT qa FROM QuestionAnswer qa " +
           "WHERE qa.sessionId = :sessionId AND qa.questionType = :questionType " +
           "ORDER BY qa.answeredAt ASC")
    List<QuestionAnswer> findBySessionIdAndQuestionType(@Param("sessionId") String sessionId, 
                                                       @Param("questionType") String questionType);

    /**
     * 사용자 ID로 문제 유형별 통계 조회
     * 전체 학습에서 문제 유형별 성과 분석에 사용
     */
    @Query("SELECT qa.questionType, " +
           "COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "GROUP BY qa.questionType " +
           "ORDER BY qa.questionType")
    List<Object[]> getQuestionTypeStatsByUserId(@Param("userId") String userId);

    /**
     * 세션 ID로 문제 유형별 통계 조회
     * 특정 세션의 문제 유형별 성과 분석에 사용
     */
    @Query("SELECT qa.questionType, " +
           "COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent " +
           "FROM QuestionAnswer qa " +
           "WHERE qa.sessionId = :sessionId " +
           "GROUP BY qa.questionType " +
           "ORDER BY qa.questionType")
    List<Object[]> getQuestionTypeStatsBySessionId(@Param("sessionId") String sessionId);

    /**
     * 사용자 ID와 날짜 범위로 문제 유형별 통계 조회
     * 특정 기간의 문제 유형별 성과 분석에 사용
     */
    @Query("SELECT qa.questionType, " +
           "COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(CASE WHEN qa.isCorrect = false THEN 1 ELSE 0 END) as wrongAnswers, " +
           "AVG(qa.timeSpent) as averageTimeSpent " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.answeredAt >= :startDate " +
           "AND qa.answeredAt <= :endDate " +
           "GROUP BY qa.questionType " +
           "ORDER BY qa.questionType")
    List<Object[]> getQuestionTypeStatsByUserIdAndDateRange(@Param("userId") String userId, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);

    // ===== 학습 시간 통계를 위한 최적화된 쿼리 메서드들 =====

    /**
     * 사용자 ID로 총 학습 시간 조회 (초 단위)
     * 모든 세션의 timeSpent 합계를 한 번의 쿼리로 조회
     */
    @Query("SELECT COALESCE(SUM(qa.timeSpent), 0) FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId AND qa.timeSpent IS NOT NULL")
    Long getTotalLearningTimeByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID와 날짜 범위로 총 학습 시간 조회 (초 단위)
     * 특정 기간의 timeSpent 합계를 한 번의 쿼리로 조회
     */
    @Query("SELECT COALESCE(SUM(qa.timeSpent), 0) FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.timeSpent IS NOT NULL " +
           "AND qa.answeredAt >= :startDate " +
           "AND qa.answeredAt <= :endDate")
    Long getTotalLearningTimeByUserIdAndDateRange(@Param("userId") String userId, 
                                                 @Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자 ID로 일별 학습 시간 집계 조회
     * 일별로 timeSpent 합계를 그룹화하여 조회
     */
    @Query("SELECT DATE(qa.answeredAt) as studyDate, " +
           "COALESCE(SUM(qa.timeSpent), 0) as totalTimeSpent, " +
           "COUNT(qa) as questionCount " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.timeSpent IS NOT NULL " +
           "GROUP BY DATE(qa.answeredAt) " +
           "ORDER BY studyDate DESC")
    List<Object[]> getDailyLearningTimeByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID로 세션별 학습 시간 집계 조회
     * 세션별로 timeSpent 합계를 그룹화하여 조회
     */
    @Query("SELECT qa.sessionId, " +
           "COALESCE(SUM(qa.timeSpent), 0) as totalTimeSpent, " +
           "COUNT(qa) as questionCount, " +
           "ls.sessionType, " +
           "ls.startedAt, " +
           "ls.completedAt " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.timeSpent IS NOT NULL " +
           "GROUP BY qa.sessionId, ls.sessionType, ls.startedAt, ls.completedAt " +
           "ORDER BY ls.startedAt DESC")
    List<Object[]> getSessionLearningTimeByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID로 월별 학습 시간 집계 조회
     * 월별로 timeSpent 합계를 그룹화하여 조회
     */
    @Query("SELECT YEAR(qa.answeredAt) as studyYear, " +
           "MONTH(qa.answeredAt) as studyMonth, " +
           "COALESCE(SUM(qa.timeSpent), 0) as totalTimeSpent, " +
           "COUNT(qa) as questionCount " +
           "FROM QuestionAnswer qa " +
           "JOIN LearningSession ls ON qa.sessionId = ls.sessionId " +
           "WHERE ls.userId = :userId " +
           "AND qa.timeSpent IS NOT NULL " +
           "GROUP BY YEAR(qa.answeredAt), MONTH(qa.answeredAt) " +
           "ORDER BY studyYear DESC, studyMonth DESC")
    List<Object[]> getMonthlyLearningTimeByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID로 세션 타입별 통계 조회
     * 세션 타입별로 정답률, 완료된 세션 수, 총 문제 수 등을 집계
     */
    @Query("SELECT CAST(ls.sessionType AS string), " +
           "COUNT(DISTINCT ls.sessionId) as totalSessions, " +
           "COUNT(CASE WHEN ls.status = 'COMPLETED' THEN 1 END) as completedSessions, " +
           "COUNT(qa) as totalQuestions, " +
           "SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) as correctAnswers, " +
           "SUM(qa.timeSpent) as totalTimeSpent " +
           "FROM LearningSession ls " +
           "LEFT JOIN QuestionAnswer qa ON ls.sessionId = qa.sessionId " +
           "WHERE ls.userId = :userId " +
           "GROUP BY ls.sessionType " +
           "ORDER BY ls.sessionType")
    List<Object[]> getSessionTypeStatsByUserId(@Param("userId") String userId);
}
