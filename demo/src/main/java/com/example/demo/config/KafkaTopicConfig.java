package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic learningSessionEvents() {
        return TopicBuilder.name("learning-session-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic questionAnswerEvents() {
        return TopicBuilder.name("question-answer-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userProgressEvents() {
        return TopicBuilder.name("user-progress-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
