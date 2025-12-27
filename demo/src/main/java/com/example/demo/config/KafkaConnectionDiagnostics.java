package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 연결 상태 진단 컴포넌트
 * 애플리케이션 시작 시 Kafka 연결 상태를 확인하고 로그로 출력
 */
@Slf4j
@Component
public class KafkaConnectionDiagnostics {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;
    
    @EventListener(ApplicationReadyEvent.class)
    public void checkKafkaConnection() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🔍 Kafka 연결 상태 진단 시작...");
        log.info("   Bootstrap Servers: {}", bootstrapServers);
        log.info("   예상 포트 매핑: localhost:9094 → 컨테이너 내부 9093 (PLAINTEXT_HOST)");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        AdminClient adminClient = null;
        try {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000); // 타임아웃 증가
            props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 10000);
            
            adminClient = AdminClient.create(props);
            
            log.info("┌─ Kafka AdminClient 생성 완료");
            log.info("   Bootstrap Servers: {}", bootstrapServers);
            
            // 토픽 목록 조회 시도
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topics = topicsResult.names().get(10, TimeUnit.SECONDS); // 타임아웃 증가
            
            log.info("└─ ✅ Kafka 연결 성공!");
            log.info("   조회된 토픽 수: {}", topics.size());
            if (!topics.isEmpty()) {
                log.info("   토픽 목록: {}", topics);
            }
            
            // learning-session-completed 토픽 확인
            if (topics.contains("learning-session-completed")) {
                log.info("   ✅ 'learning-session-completed' 토픽 존재 확인");
            } else {
                log.warn("   ⚠️  'learning-session-completed' 토픽이 존재하지 않습니다");
            }
            
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("❌ Kafka 연결 타임아웃!");
            log.error("   Bootstrap Servers: {}", bootstrapServers);
            log.error("   에러: {}", e.getMessage());
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("확인 사항:");
            log.error("1. Kafka 컨테이너 실행 확인: docker ps | grep kafka");
            log.error("2. Kafka 컨테이너 로그 확인: docker logs kafka | tail -50");
            log.error("3. 포트 리스닝 확인: lsof -i :9094 또는 netstat -an | grep 9094");
            log.error("4. Kafka 컨테이너 재시작: docker restart kafka");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } catch (Exception e) {
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("❌ Kafka 연결 실패!");
            log.error("   Bootstrap Servers: {}", bootstrapServers);
            log.error("   에러 타입: {}", e.getClass().getSimpleName());
            log.error("   에러 메시지: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("   원인: {}", e.getCause().getMessage());
            }
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.error("해결 방법:");
            log.error("1. Kafka 컨테이너가 실행 중인지 확인: docker ps | grep kafka");
            log.error("2. Kafka 컨테이너 로그 확인: docker logs kafka | tail -50");
            log.error("3. 포트 리스닝 확인: lsof -i :9094");
            log.error("4. 환경 변수 확인: echo $KAFKA_BOOTSTRAP_SERVERS");
            log.error("5. Kafka 컨테이너 재시작: docker restart kafka");
            log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        } finally {
            if (adminClient != null) {
                adminClient.close();
            }
        }
    }
}

