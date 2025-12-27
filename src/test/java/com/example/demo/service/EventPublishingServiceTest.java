package com.example.demo.service;

import com.example.demo.dto.LearningSessionCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublishingServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EventPublishingService eventPublishingService;

    private LearningSessionCompletedEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = LearningSessionCompletedEvent.builder()
            .sessionId("test-session-123")
            .userId("test-user-456")
            .learningItemId("FILL_IN_THE_BLANK")
            .totalQuestions(10)
            .answeredQuestions(10)
            .correctAnswers(8)
            .wrongAnswers(2)
            .score(80)
            .totalDuration(600) // 10분
            .startedAt(LocalDateTime.now().minusMinutes(10))
            .completedAt(LocalDateTime.now())

            .timestamp(LocalDateTime.now())
            .build();
    }

    @Test
    void publishLearningSessionCompletedEvent_정상발행() {
        // given
        SendResult<String, Object> mockResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mockResult);
        
        when(kafkaTemplate.send(anyString(), anyString(), any(LearningSessionCompletedEvent.class)))
            .thenReturn(future);

        // when
        eventPublishingService.publishLearningSessionCompletedEvent(testEvent);

        // then
        verify(kafkaTemplate, times(1))
            .send(eq("learning-session-completed-events"), eq("test-session-123"), eq(testEvent));
    }

    @Test
    void publishLearningSessionCompletedEvent_Kafka발행실패시_예외처리() {
        // given
        when(kafkaTemplate.send(anyString(), anyString(), any(LearningSessionCompletedEvent.class)))
            .thenThrow(new RuntimeException("Kafka 연결 실패"));

        // when & then
        // 예외가 발생해도 서비스가 중단되지 않아야 함
        eventPublishingService.publishLearningSessionCompletedEvent(testEvent);
        
        // 로그만 남기고 정상 종료되어야 함
        verify(kafkaTemplate, times(1))
            .send(anyString(), anyString(), any(LearningSessionCompletedEvent.class));
    }

    @Test
    void isEventPublishingAvailable_Kafka연결상태확인() {
        // given
        when(kafkaTemplate.getDefaultTopic()).thenReturn("test-topic");

        // when
        boolean isAvailable = eventPublishingService.isEventPublishingAvailable();

        // then
        assertThat(isAvailable).isTrue();
        verify(kafkaTemplate, times(1)).getDefaultTopic();
    }

    @Test
    void isEventPublishingAvailable_Kafka연결실패시_false반환() {
        // given
        when(kafkaTemplate.getDefaultTopic()).thenThrow(new RuntimeException("연결 실패"));

        // when
        boolean isAvailable = eventPublishingService.isEventPublishingAvailable();

        // then
        assertThat(isAvailable).isFalse();
        verify(kafkaTemplate, times(1)).getDefaultTopic();
    }
}
