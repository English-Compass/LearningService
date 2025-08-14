package com.example.demo.repository;

import com.example.demo.entity.LearningEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningEventRepository extends JpaRepository<LearningEvent, Long> {
    
    List<LearningEvent> findByUserIdAndLearningItemIdOrderByTimestampAsc(String userId, String learningItemId);
    
    List<LearningEvent> findByUserIdAndLearningItemIdAndTimestampBetweenOrderByTimestampAsc(
        String userId, String learningItemId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT DISTINCT l.learningItemId FROM LearningEvent l WHERE l.userId = :userId")
    List<String> findDistinctLearningItemIdsByUserId(@Param("userId") String userId);
    
    @Query("SELECT DISTINCT l.userId FROM LearningEvent l WHERE l.learningItemId = :learningItemId")
    List<String> findDistinctUserIdsByLearningItemId(@Param("learningItemId") String learningItemId);
    
    @Query("SELECT l FROM LearningEvent l WHERE l.userId = :userId ORDER BY l.timestamp DESC")
    List<LearningEvent> findRecentEventsByUserId(@Param("userId") String userId);
    
    // 페이징을 위한 최근 이벤트 조회
    @Query("SELECT l FROM LearningEvent l WHERE l.userId = :userId ORDER BY l.timestamp DESC")
    List<LearningEvent> findRecentEventsByUserIdWithLimit(@Param("userId") String userId, int limit);
}
