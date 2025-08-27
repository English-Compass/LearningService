package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSession {
    @Id
    private String sessionId; // 기본키로 사용

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성 시간

    @Column(nullable = false)
    private LocalDateTime updatedAt; // 업데이트 시간

    @Column
    private LocalDateTime completedAt; // 완료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status; // 세션 상태

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType; // 세션 타입

    @Column(columnDefinition = "TEXT")
    private String sessionMetadata; // 세션 메타데이터 (JSON)
    
    @Column
    private LocalDateTime startedAt; // 세션 시작 시간

    public enum SessionType {
        PRACTICE("문제 세션"),
        REVIEW("복습 세션"),
        WRONG_ANSWER("오답 세션");
        
        private final String displayName;
        
        SessionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SessionStatus {
        STARTED,        // 세션 시작
        IN_PROGRESS,    // 진행 중
        COMPLETED       // 완료
    }

    // 세션 시작 처리
    public void startSession() {
        this.status = SessionStatus.STARTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 세션 진행 중으로 변경
    public void setInProgress() {
        this.status = SessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    // 세션 완료 처리
    public void completeSession() {
        this.completedAt = LocalDateTime.now();
        this.status = SessionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
    

    // 세션 시작 시간 설정
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        this.updatedAt = LocalDateTime.now();
    }

}
