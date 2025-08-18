package com.example.demo.repository;

import com.example.demo.entity.LearningSessionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearningSessionEventRepository extends JpaRepository<LearningSessionEvent, String> {
    
    // 세션 ID로 이벤트 조회
    Optional<LearningSessionEvent> findBySessionId(String sessionId);
    
    // 사용자 ID로 이벤트 목록 조회
    List<LearningSessionEvent> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // 이벤트 상태별 조회
    List<LearningSessionEvent> findByEventStatusOrderByCreatedAtAsc(String eventStatus);
    
    // 발행 대기 중인 이벤트 조회 (재처리용)
    List<LearningSessionEvent> findByEventStatusAndCreatedAtBefore(String eventStatus, LocalDateTime before);
    
    // 발행 실패한 이벤트 조회 (재처리용)
    List<LearningSessionEvent> findByEventStatusAndPublishErrorIsNotNull(String eventStatus);
    
    // 특정 기간 동안의 이벤트 조회
    @Query("SELECT e FROM LearningSessionEvent e WHERE e.createdAt BETWEEN :startDate AND :endDate ORDER BY e.createdAt DESC")
    List<LearningSessionEvent> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 사용자별 학습 패턴 조회 (최근 N개)
    @Query("SELECT e FROM LearningSessionEvent e WHERE e.userId = :userId AND e.eventType = 'SESSION_COMPLETED' ORDER BY e.createdAt DESC")
    List<LearningSessionEvent> findRecentCompletedSessionsByUserId(@Param("userId") String userId);
    
    // 카테고리별 성과 분석을 위한 이벤트 조회
    @Query("SELECT e FROM LearningSessionEvent e WHERE e.userId = :userId AND e.eventType = 'SESSION_COMPLETED' AND e.categoryAccuracy LIKE %:category% ORDER BY e.createdAt DESC")
    List<LearningSessionEvent> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);
    
    // 학습 패턴별 이벤트 조회
    List<LearningSessionEvent> findByUserIdAndLearningPatternOrderByCreatedAtDesc(String userId, String learningPattern);
    
    // 점수 범위별 이벤트 조회
    @Query("SELECT e FROM LearningSessionEvent e WHERE e.userId = :userId AND e.score BETWEEN :minScore AND :maxScore ORDER BY e.createdAt DESC")
    List<LearningSessionEvent> findByUserIdAndScoreRange(@Param("userId") String userId, @Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore);
    
    // 특정 세션 타입의 완료 이벤트 조회
    @Query("SELECT e FROM LearningSessionEvent e JOIN e.learningSession ls WHERE ls.sessionType = :sessionType AND e.eventType = 'SESSION_COMPLETED' ORDER BY e.createdAt DESC")
    List<LearningSessionEvent> findBySessionType(@Param("sessionType") String sessionType);
    
    // 이벤트 발행 상태 통계
    @Query("SELECT e.eventStatus, COUNT(e) FROM LearningSessionEvent e GROUP BY e.eventStatus")
    List<Object[]> countByEventStatus();
    
    // 사용자별 이벤트 발행 성공률
    @Query("SELECT e.userId, COUNT(e) as total, SUM(CASE WHEN e.eventStatus = 'PUBLISHED' THEN 1 ELSE 0 END) as published FROM LearningSessionEvent e GROUP BY e.userId")
    List<Object[]> getUserEventPublishRate();
}
