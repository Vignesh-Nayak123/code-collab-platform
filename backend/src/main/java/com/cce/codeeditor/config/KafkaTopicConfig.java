package com.cce.codeeditor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic executionRequestsTopic() {
        return TopicBuilder.name("execution-requests")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic executionResultsTopic() {
        return TopicBuilder.name("execution-results")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
