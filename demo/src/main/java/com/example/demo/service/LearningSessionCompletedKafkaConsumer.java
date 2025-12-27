package com.example.demo.service;

import com.example.demo.dto.LearningCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학습 세션 완료 이벤트 Kafka 컨슈머
 * learning-session-completed 토픽을 구독하여 메시지를 수신하고 분석 로직을 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningSessionCompletedKafkaConsumer {

    private final LearningSessionEventListener learningSessionEventListener;

    /**
     * learning-session-completed 토픽 구독 및 메시지 처리
     * 메시지 수신 시 기존 분석 로직을 실행
     * 
     * @param event 학습 완료 이벤트
     * @param acknowledgment Kafka offset 커밋을 위한 acknowledgment
     * @param partition 파티션 번호
     * @param offset 오프셋
     */
    @KafkaListener(
        topics = "learning-session-completed",
        groupId = "learning-service-analysis-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeLearningSessionCompleted(
            @Payload LearningCompletedEvent event,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        long startTime = System.currentTimeMillis();
        String sessionId = event.getSessionId();
        String userId = event.getUserId();
        
        try {
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📥 [분석 시작] Kafka에서 학습 세션 완료 이벤트 수신");
            log.info("   sessionId={}, userId={}, partition={}, offset={}", 
                sessionId, userId, partition, offset);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 기존 분석 로직 실행
            learningSessionEventListener.handleLearningSessionCompleted(event);
            
            // 성공적으로 처리된 경우 offset 커밋
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.debug("Kafka offset 커밋 완료: partition={}, offset={}", partition, offset);
            }
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("✅ [분석 완료] 학습 세션 완료 이벤트 처리 완료");
            log.info("   sessionId={}, userId={}, 소요시간={}ms", 
                sessionId, userId, elapsedTime);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("❌ [분석 실패] Kafka 메시지 처리 실패");
            log.error("   sessionId={}, userId={}, partition={}, offset={}, 소요시간={}ms", 
                sessionId, userId, partition, offset, elapsedTime, e);
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 에러 발생 시에도 offset을 커밋하여 무한 재시도를 방지
            // 실제 운영 환경에서는 DLQ(Dead Letter Queue)로 전송하거나 재시도 로직을 구현하는 것이 좋음
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.warn("에러 발생 후에도 offset 커밋: partition={}, offset={}", partition, offset);
            }
            
            // TODO: DLQ로 전송하거나 재시도 로직 구현
            // 예외를 다시 던지지 않아서 컨슈머가 계속 동작하도록 함
        }
    }
}

