package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.config.LearningCompletedEventDeserializer;

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
    
    private static final Logger log = LoggerFactory.getLogger(KafkaTopicConfig.class);

    /**
     * Kafka 프로듀서 팩토리 설정
     * - JSON 직렬화를 사용하여 이벤트 객체를 Kafka로 전송
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        log.info("🔧 Kafka 프로듀서 팩토리 생성 중... Bootstrap Servers: {}", bootstrapServers);
        
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // 로컬 실행 시 추가 설정
        if (bootstrapServers.contains("localhost") || bootstrapServers.contains("127.0.0.1")) {
            configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
            configProps.put(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
        }
        
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
     * Kafka 컨슈머 팩토리 설정
     * - JSON 역직렬화를 사용하여 Kafka 메시지를 객체로 변환
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔧 Kafka 컨슈머 팩토리 생성 중...");
        log.info("   Bootstrap Servers: {}", bootstrapServers);
        log.info("   Consumer Group ID: learning-service-analysis-group");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "learning-service-analysis-group");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // ErrorHandlingDeserializer로 래핑하여 역직렬화 에러 처리
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        // 커스텀 역직렬화기 사용: 타입 정보를 무시하고 LearningCompletedEvent DTO로 직접 변환
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, LearningCompletedEventDeserializer.class);
        
        // 로컬에서 실행 시 advertised listener 문제 해결
        // Kafka가 kafka:9092를 반환해도 localhost:9094를 계속 사용하도록 설정
        if (bootstrapServers.contains("localhost") || bootstrapServers.contains("127.0.0.1")) {
            // 로컬 실행 시: advertised listener를 무시하고 bootstrap servers를 계속 사용
            configProps.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 300000); // 5분 (메타데이터 캐싱 시간 증가)
            configProps.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
            // 연결 타임아웃 설정
            configProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
            configProps.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000);
            // 재시도 설정
            configProps.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
            log.info("   ⚙️  로컬 실행 모드: advertised listener 우회 설정 적용");
        }
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka 리스너 컨테이너 팩토리 설정
     * - 수동 acknowledgment 모드 사용
     * - 동시성 설정 (여러 파티션 병렬 처리)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        log.info("🔧 Kafka 리스너 컨테이너 팩토리 생성 중...");
        
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 수동 acknowledgment 모드 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // 동시성 설정 (파티션 수에 맞춰 조정 가능)
        factory.setConcurrency(3);
        
        // 연결 상태 로깅을 위한 리스너 추가
        factory.getContainerProperties().setConsumerStartTimeout(java.time.Duration.ofSeconds(30));
        
        // 역직렬화 에러 처리: ErrorHandlingDeserializer 사용
        // SerializationException을 처리할 수 있도록 설정
        factory.setCommonErrorHandler(
            new org.springframework.kafka.listener.DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Kafka 메시지 처리 실패: topic={}, partition={}, offset={}, key={}",
                        record.topic(), record.partition(), record.offset(), record.key(), exception);
                }
            )
        );
        
        log.info("   ✅ 리스너 컨테이너 팩토리 생성 완료 (동시성: 3)");
        return factory;
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

    /**
     * learning-session-completed 토픽 설정
     * - 실제 사용되는 토픽 이름
     */
    @Bean
    public NewTopic learningSessionCompleted() {
        return TopicBuilder.name("learning-session-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * learning-analysis-completed 토픽 설정
     * - 분석 완료 이벤트 발행용 토픽
     * - ProblemService에서 구독하여 REST API 호출
     */
    @Bean
    public NewTopic learningAnalysisCompleted() {
        return TopicBuilder.name("learning-analysis-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}