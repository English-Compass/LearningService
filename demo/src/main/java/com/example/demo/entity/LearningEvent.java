package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String eventType; // START, COMPLETE, PAUSE, RESUME 등
    
    @Column(nullable = false)
    private String learningItemId; // 학습 항목 ID
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private Integer duration; // 학습 시간(초)
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // 추가 메타데이터 (JSON 형태)
}
