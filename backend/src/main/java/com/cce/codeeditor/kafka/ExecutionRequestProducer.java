package com.cce.codeeditor.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionRequestProducer {

    private static final String TOPIC = "execution-requests";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendExecutionRequest(String key, String payload) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Sent execution request: key={}, offset={}",
                        key, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send execution request: key={}", key, ex);
            }
        });
    }
}
