package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학습 세션 이벤트 엔티티
 * 세션 완료 시 발생하는 이벤트를 기록하여 영속성 보장
 * 복잡한 상태 관리나 세션 정보는 제거하고 핵심 정보만 유지
 */
@Entity
@Table(name = "learning_session_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSessionEvent {
    
    @Id
    private String eventId; // 이벤트 고유 ID
    
    @Column(nullable = false)
    private String sessionId; // 학습 세션 ID (FK)
    
    @Column(nullable = false)
    private String userId; // 사용자 ID
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // 이벤트 생성 시간
    
    @Column(nullable = false)
    private String eventType; // 이벤트 타입 (SESSION_COMPLETED, SESSION_STARTED 등) - 향후 확장성을 고려해서 필드 유지
    
    // === 인덱스용 필드들 ===
    @Column
    private String sessionType; // 세션 타입 (조회 성능 향상을 위한 인덱스) - 향후 확장성을 고려해서 필드 유지
    
    // === 메타데이터 ===
    @Column(columnDefinition = "TEXT")
    private String eventMetadata; // 이벤트 관련 추가 메타데이터 (JSON) - 새로운 메타데이터 추가시 유연성을 고려해서 필드 유지
}
