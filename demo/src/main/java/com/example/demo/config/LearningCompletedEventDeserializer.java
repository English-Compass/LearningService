package com.example.demo.config;

import com.example.demo.dto.LearningCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 * LearningCompletedEvent 전용 커스텀 역직렬화기
 * 타입 정보를 무시하고 JSON을 직접 파싱하여 LearningCompletedEvent DTO로 변환
 */
@Slf4j
public class LearningCompletedEventDeserializer implements Deserializer<LearningCompletedEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonDeserializer<LearningCompletedEvent> jsonDeserializer;

    public LearningCompletedEventDeserializer() {
        // 타입 정보를 무시하고 항상 LearningCompletedEvent로 역직렬화
        this.jsonDeserializer = new JsonDeserializer<>(LearningCompletedEvent.class);
        this.jsonDeserializer.setUseTypeHeaders(false); // 타입 헤더 사용 안 함
        this.jsonDeserializer.setRemoveTypeHeaders(true); // 타입 헤더 제거
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        jsonDeserializer.configure(configs, isKey);
    }

    @Override
    public LearningCompletedEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            // 타입 정보를 무시하고 JSON을 직접 파싱하여 LearningCompletedEvent로 변환
            return jsonDeserializer.deserialize(topic, data);
        } catch (Exception e) {
            log.error("LearningCompletedEvent 역직렬화 실패: topic={}, error={}", topic, e.getMessage(), e);
            // JSON을 직접 파싱 시도
            try {
                return objectMapper.readValue(data, LearningCompletedEvent.class);
            } catch (Exception ex) {
                log.error("JSON 직접 파싱도 실패: topic={}", topic, ex);
                throw new RuntimeException("LearningCompletedEvent 역직렬화 실패", ex);
            }
        }
    }

    @Override
    public void close() {
        jsonDeserializer.close();
    }
}

