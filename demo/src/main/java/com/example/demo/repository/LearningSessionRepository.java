package com.example.demo.repository;

import com.example.demo.entity.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 학습 세션 Repository
 * 학습 세션 정보를 관리
 */
@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, String> {

    /**
     * 사용자 ID로 세션 목록 조회
     */
    List<LearningSession> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 사용자 ID와 세션 상태로 세션 목록 조회
     */
    List<LearningSession> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, LearningSession.SessionStatus status);

    /**
     * 사용자 ID와 세션 타입으로 세션 목록 조회
     */
    List<LearningSession> findByUserIdAndSessionTypeOrderByCreatedAtDesc(String userId, LearningSession.SessionType sessionType);

    /**
     * 사용자 ID와 날짜 범위로 세션 목록 조회 (전체 기간용)
     */
    List<LearningSession> findByUserIdAndStartedAtBetweenOrderByCreatedAtDesc(String userId, 
                                                                           LocalDateTime startDate, 
                                                                           LocalDateTime endDate);

    /**
     * 사용자 ID와 주차별 세션 목록 조회 (주간 통계용)
     */
    @Query("SELECT ls FROM LearningSession ls WHERE ls.userId = :userId " +
           "AND ls.startedAt >= :startDate AND ls.startedAt <= :endDate " +
           "ORDER BY ls.startedAt ASC")
    List<LearningSession> findWeeklySessionsByUserIdAndDateRange(@Param("userId") String userId, 
                                                               @Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자 ID와 월별 세션 목록 조회 (월간 통계용)
     */
    @Query("SELECT ls FROM LearningSession ls WHERE ls.userId = :userId " +
           "AND ls.startedAt >= :startDate AND ls.startedAt <= :endDate " +
           "ORDER BY ls.startedAt ASC")
    List<LearningSession> findMonthlySessionsByUserIdAndDateRange(@Param("userId") String userId, 
                                                                @Param("startDate") LocalDateTime startDate, 
                                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 세션 상태로 세션 목록 조회
     */
    List<LearningSession> findByStatusOrderByCreatedAtDesc(LearningSession.SessionStatus status);

    /**
     * 세션 타입으로 세션 목록 조회
     */
    List<LearningSession> findBySessionTypeOrderByCreatedAtDesc(LearningSession.SessionType sessionType);

    /**
     * 완료된 세션 목록 조회
     */
    @Query("SELECT ls FROM LearningSession ls WHERE ls.status = 'COMPLETED' ORDER BY ls.completedAt DESC")
    List<LearningSession> findCompletedSessions();

    /**
     * 사용자 ID로 완료된 세션 개수 조회
     */
    @Query("SELECT COUNT(ls) FROM LearningSession ls WHERE ls.userId = :userId AND ls.status = 'COMPLETED'")
    long countCompletedSessionsByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID로 진행 중인 세션 개수 조회
     */
    @Query("SELECT COUNT(ls) FROM LearningSession ls WHERE ls.userId = :userId AND ls.status = 'IN_PROGRESS'")
    long countInProgressSessionsByUserId(@Param("userId") String userId);
}
