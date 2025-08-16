package com.example.demo.service;

import com.example.demo.dto.LearningCompletedEvent;
import com.example.demo.dto.LearningSessionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이벤트 발행 서비스
 * 학습 완료 이벤트만 발행하여 다른 시스템에 알림
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublishingService {

    private final RedisCacheService redisCacheService;

    // TODO: 실제 구현 시 Kafka, RabbitMQ 등의 메시지 큐 사용
    // private final KafkaTemplate<String, Object> kafkaTemplate;
    // private final RabbitTemplate rabbitTemplate;

    /**
     * 학습 완료 이벤트 생성 및 발행
     */
    public void publishLearningCompletedEvent(LearningSessionCompletedEvent completedEvent) {
        try {
            // 1. 완료 이벤트 정보를 기반으로 이벤트 생성
            LearningCompletedEvent event = LearningCompletedEvent.builder()
                .sessionId(completedEvent.getSessionId())
                .userId(completedEvent.getUserId())
                .learningItemId(completedEvent.getLearningItemId())
                .totalQuestions(completedEvent.getTotalQuestions())
                .answeredQuestions(completedEvent.getAnsweredQuestions())
                .correctAnswers(completedEvent.getCorrectAnswers())
                .wrongAnswers(completedEvent.getWrongAnswers())
                .score(completedEvent.getScore())
                .startedAt(completedEvent.getStartedAt())
                .completedAt(completedEvent.getCompletedAt())
                .build();

            // 2. 이벤트 발행
            publishLearningCompletedEvent(event);
            
        } catch (Exception e) {
            log.error("학습 완료 이벤트 생성 및 발행 실패: sessionId={}", completedEvent.getSessionId(), e);
        }
    }

    /**
     * 학습 완료 이벤트 발행
     * 전체 학습 결과 분석 및 오답/복습 문제 제공을 위한 데이터 수집
     */
    public void publishLearningCompletedEvent(LearningCompletedEvent event) {
        try {
            // 이벤트 ID 생성
            event.setEventId(UUID.randomUUID().toString());
            event.setTimestamp(LocalDateTime.now());
            
            // 이벤트를 캐시에 저장 (이벤트 조회용)
            redisCacheService.cacheEvent("learning-completed", event.getEventId(), event);
            
            // TODO: Kafka 토픽으로 이벤트 발행
            // kafkaTemplate.send("learning-completed", event.getUserId(), event);
            
            // TODO: RabbitMQ로 이벤트 발행
            // rabbitTemplate.convertAndSend("learning.exchange", "learning.completed", event);
            
            log.info("학습 완료 이벤트 발행 완료: eventId={}, sessionId={}, userId={}, score={}, totalQuestions={}", 
                event.getEventId(), event.getSessionId(), event.getUserId(), event.getScore(), event.getTotalQuestions());
            
        } catch (Exception e) {
            log.error("학습 완료 이벤트 발행 실패: sessionId={}", event.getSessionId(), e);
        }
    }

    /**
     * 이벤트 발행 상태 확인
     */
    public boolean isEventPublishingAvailable() {
        // TODO: 실제 구현 시 메시지 큐 연결 상태 확인
        return true; // 임시로 항상 true 반환
    }
}
