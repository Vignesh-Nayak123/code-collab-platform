package com.cce.codeeditor.kafka;

import com.cce.codeeditor.execution.DockerExecutionService;
import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.enums.ExecutionStatus;
import com.cce.codeeditor.repository.ExecutionResultRepository;
import com.cce.codeeditor.service.CodeExecutionServiceImpl.ExecutionPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionResultConsumer {

    private final ExecutionResultRepository executionResultRepository;
    private final DockerExecutionService dockerExecutionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "execution-requests", groupId = "code-execution-group")
    public void consumeExecutionRequest(String payload) {
        try {
            ExecutionPayload executionPayload = objectMapper.readValue(payload, ExecutionPayload.class);
            log.info("Processing execution request: {}", executionPayload.executionId());

            Optional<ExecutionResult> resultOpt = executionResultRepository.findById(executionPayload.executionId());
            if (resultOpt.isEmpty()) {
                log.warn("Execution result not found: {}", executionPayload.executionId());
                return;
            }

            ExecutionResult result = resultOpt.get();
            result.setStatus(ExecutionStatus.RUNNING);
            executionResultRepository.save(result);

            dockerExecutionService.execute(result, executionPayload.request().getCode(),
                    executionPayload.request().getLanguage());

        } catch (Exception e) {
            log.error("Failed to process execution request", e);
        }
    }
}
