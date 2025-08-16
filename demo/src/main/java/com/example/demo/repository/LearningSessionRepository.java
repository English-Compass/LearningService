package com.example.demo.repository;

import com.example.demo.entity.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, String> {
    
    // 세션 ID로 조회
    Optional<LearningSession> findBySessionId(String sessionId);
    
    // 사용자 ID로 세션 목록 조회
    List<LearningSession> findByUserIdOrderByStartedAtDesc(String userId);
    
    // 사용자 ID로 세션 목록 조회 (상태별)
    List<LearningSession> findByUserIdAndStatus(String userId, LearningSession.SessionStatus status);
    
    // 사용자 ID와 상태로 세션 목록 조회 (시작 시간 역순)
    List<LearningSession> findByUserIdAndStatusOrderByStartedAtDesc(String userId, LearningSession.SessionStatus status);
    
    // 사용자 ID와 여러 상태로 세션 목록 조회 (시작 시간 역순)
    List<LearningSession> findByUserIdAndStatusInOrderByStartedAtDesc(String userId, List<LearningSession.SessionStatus> statuses);
    
    // 특정 기간 내 사용자의 세션 조회
    @Query("SELECT s FROM LearningSession s WHERE s.userId = :userId AND s.startedAt BETWEEN :startDate AND :endDate ORDER BY s.startedAt DESC")
    List<LearningSession> findByUserIdAndPeriod(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // 완료된 세션만 조회
    List<LearningSession> findByUserIdAndStatusOrderByCompletedAtDesc(String userId, LearningSession.SessionStatus status);
    
    // 학습 항목별 세션 조회
    List<LearningSession> findByLearningItemIdOrderByStartedAtDesc(String learningItemId);
    
    // 사용자별 세션 수
    @Query("SELECT COUNT(s) FROM LearningSession s WHERE s.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    // 사용자별 완료된 세션 수
    @Query("SELECT COUNT(s) FROM LearningSession s WHERE s.userId = :userId AND s.status = 'COMPLETED'")
    Long countCompletedByUserId(@Param("userId") String userId);
    

}
