package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 학습 세션 이벤트 엔티티
 * 세션 완료 시 즉시 발행되는 이벤트 (신호)로 사용
 * 통계 정보는 백그라운드에서 계산되어 별도 엔티티에 저장
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
    
    // === 세션 기본 정보 ===
    @Column(nullable = false)
    private String sessionType; // PRACTICE, REVIEW, WRONG_ANSWER
    
    @Column(nullable = false)
    private LocalDateTime sessionStartedAt; // 세션 시작 시간
    
    @Column(nullable = false)
    private LocalDateTime sessionCompletedAt; // 세션 완료 시간
    
    @Column(nullable = false)
    private String sessionStatus; // COMPLETED, ABANDONED, PAUSED
    
    @Column(columnDefinition = "TEXT")
    private String sessionMetadata; // 세션 메타데이터 (JSON)
    
    // === 이벤트 메타데이터 ===
    @Column(nullable = false)
    private String eventType; // 이벤트 타입 (SESSION_COMPLETED, SESSION_ABANDONED, SESSION_PAUSED)
    
    @Column(nullable = false)
    private String eventStatus; // 이벤트 상태 (PENDING, PUBLISHED, FAILED)
    
    @Column
    private String eventVersion; // 이벤트 버전 (향후 호환성)
    
    @Column
    private LocalDateTime publishedAt; // 이벤트 발행 시간
    
    @Column
    private String publishError; // 발행 실패 시 에러 메시지
    
    // === 관계 매핑 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private LearningSession learningSession;
    
    // === 이벤트 상태 열거형 ===
    public enum EventStatus {
        PENDING,    // 발행 대기
        PUBLISHED,  // 발행 완료
        FAILED      // 발행 실패
    }
    
    // === 이벤트 타입 열거형 ===
    public enum EventType {
        SESSION_COMPLETED("세션 완료"),
        SESSION_ABANDONED("세션 중단"),
        SESSION_PAUSED("세션 일시정지");
        
        private final String displayName;
        
        EventType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // === 유틸리티 메서드 ===
    
    // 이벤트 발행 성공 처리
    public void markAsPublished() {
        this.eventStatus = EventStatus.PUBLISHED.name();
        this.publishedAt = LocalDateTime.now();
    }
    
    // 이벤트 발행 실패 처리
    public void markAsFailed(String errorMessage) {
        this.eventStatus = EventStatus.FAILED.name();
        this.publishError = errorMessage;
    }
    
    // 이벤트 재발행 준비
    public void prepareForRepublish() {
        this.eventStatus = EventStatus.PENDING.name();
        this.publishedAt = null;
        this.publishError = null;
    }
    
    // 이벤트가 발행 대기 상태인지 확인
    public boolean isPending() {
        return EventStatus.PENDING.name().equals(this.eventStatus);
    }
    
    // 이벤트가 발행 완료 상태인지 확인
    public boolean isPublished() {
        return EventStatus.PUBLISHED.name().equals(this.eventStatus);
    }
    
    // 이벤트가 발행 실패 상태인지 확인
    public boolean isFailed() {
        return EventStatus.FAILED.name().equals(this.eventStatus);
    }
    
    // 이벤트가 세션 완료 타입인지 확인
    public boolean isSessionCompleted() {
        return EventType.SESSION_COMPLETED.name().equals(this.eventType);
    }
    
    // 이벤트가 세션 중단 타입인지 확인
    public boolean isSessionAbandoned() {
        return EventType.SESSION_ABANDONED.name().equals(this.eventType);
    }
    
    // 이벤트가 세션 일시정지 타입인지 확인
    public boolean isSessionPaused() {
        return EventType.SESSION_PAUSED.name().equals(this.eventType);
    }
}
