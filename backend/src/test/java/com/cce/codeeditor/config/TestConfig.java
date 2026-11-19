package com.cce.codeeditor.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import com.cce.codeeditor.kafka.ExecutionRequestProducer;

@TestConfiguration
@Import(RedisTestConfig.class)
public class TestConfig {

    @Bean
    @SuppressWarnings("unchecked")
    public KafkaTemplate<String, String> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public ExecutionRequestProducer executionRequestProducer() {
        return Mockito.mock(ExecutionRequestProducer.class);
    }
}
