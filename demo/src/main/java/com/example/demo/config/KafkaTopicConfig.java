package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 토픽 설정
 * - 학습 세션 완료 이벤트 발행을 위한 프로듀서 설정
 * - 이벤트 기반 아키텍처에서 다른 서비스와의 통신을 담당
 */
@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka 프로듀서 팩토리 설정
     * - JSON 직렬화를 사용하여 이벤트 객체를 Kafka로 전송
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka 템플릿 설정
     * - 이벤트 발행을 위한 KafkaTemplate 제공
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * 학습 세션 완료 이벤트 토픽 설정
     * - 파티션 수: 5개 (높은 처리량 대응)
     * - 복제본 수: 1개 (개발 환경 기준)
     */
    @Bean
    public NewTopic learningSessionCompletedEvents() {
        return TopicBuilder.name("learning-session-completed-events")
                .partitions(5)        // 파티션 수 증가 (높은 처리량 대응)
                .replicas(1)
                .build();
    }
}