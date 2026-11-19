package com.cce.codeeditor.kafka;

import com.cce.codeeditor.config.ConditionalOnKafkaExecution;
import com.cce.codeeditor.execution.ExecutionDispatcher;
import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.service.CodeExecutionServiceImpl.ExecutionPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Default mode: publish the execution request to Kafka for {@link ExecutionResultConsumer}. */
@Component
@ConditionalOnKafkaExecution
@RequiredArgsConstructor
@Slf4j
public class KafkaExecutionDispatcher implements ExecutionDispatcher {

    private final ExecutionRequestProducer executionRequestProducer;
    private final ObjectMapper objectMapper;

    @Override
    public void dispatch(ExecutionResult result, ExecutionRequest request) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(new ExecutionPayload(result.getId(), request));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize execution request", e);
            return;
        }
        String key = result.getId().toString();
        ExecutionDispatcher.afterCommit(() -> executionRequestProducer.sendExecutionRequest(key, payload));
    }
}
