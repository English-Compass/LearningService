package com.example.demo.service;

import com.example.demo.entity.LearningSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 이벤트 발행 서비스
 * 학습 세션 완료 시에만 이벤트를 발행하여 다른 시스템에 알림
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventPublishingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topic.session-completed:learning-session-completed-events}")
    private String sessionCompletedTopic;

    /**
     * 학습 세션 완료 이벤트 발행
     */
    public void publishLearningSessionCompletedEvent(LearningSession completedSession) {
        try {
            // 세션 완료 이벤트를 Kafka로 발행
            publishToKafka(sessionCompletedTopic, completedSession.getSessionId(), completedSession);
            
            log.info("학습 세션 완료 이벤트 발행 완료: sessionId={}, userId={}, score={}, totalQuestions={}", 
                completedSession.getSessionId(), completedSession.getUserId(), completedSession.getScore(), completedSession.getTotalQuestions());
            
        } catch (Exception e) {
            log.error("학습 세션 완료 이벤트 발행 실패: sessionId={}", completedSession.getSessionId(), e);
        }
    }

    /**
     * Kafka로 이벤트 발행
     */
    private void publishToKafka(String topic, String key, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("이벤트 발행 성공: topic={}, key={}, partition={}, offset={}", 
                        topic, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("이벤트 발행 실패: topic={}, key={}", topic, key, ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 중 오류 발생: topic={}, key={}", topic, key, e);
            throw new RuntimeException("이벤트 발행 실패", e);
        }
    }

    /**
     * 이벤트 발행 상태 확인
     */
    public boolean isEventPublishingAvailable() {
        try {
            // Kafka 연결 상태 확인 (간단한 health check)
            return kafkaTemplate.getDefaultTopic() != null;
        } catch (Exception e) {
            log.warn("Kafka 연결 상태 확인 실패", e);
            return false;
        }
    }
}
