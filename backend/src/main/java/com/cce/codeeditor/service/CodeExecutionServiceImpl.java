package com.cce.codeeditor.service;

import com.cce.codeeditor.exception.RoomNotFoundException;
import com.cce.codeeditor.kafka.ExecutionRequestProducer;
import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.dto.ExecutionResponse;
import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.entity.Room;
import com.cce.codeeditor.model.enums.ExecutionStatus;
import com.cce.codeeditor.repository.ExecutionResultRepository;
import com.cce.codeeditor.repository.RoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private final ExecutionResultRepository executionResultRepository;
    private final RoomRepository roomRepository;
    private final ExecutionRequestProducer executionRequestProducer;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExecutionResponse submitExecution(ExecutionRequest request) {
        Room room = roomRepository.findByRoomCode(request.getRoomCode())
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + request.getRoomCode()));

        ExecutionResult result = ExecutionResult.builder()
                .roomId(room.getId())
                .language(request.getLanguage())
                .code(request.getCode())
                .status(ExecutionStatus.PENDING)
                .submittedBy(request.getSubmittedBy())
                .build();

        result = executionResultRepository.save(result);

        try {
            String payload = objectMapper.writeValueAsString(new ExecutionPayload(result.getId(), request));
            executionRequestProducer.sendExecutionRequest(result.getId().toString(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize execution request", e);
        }

        return ExecutionResponse.builder()
                .executionId(result.getId())
                .status(result.getStatus().name())
                .build();
    }

    @Override
    public ExecutionResponse getResult(UUID executionId) {
        ExecutionResult result = executionResultRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        return ExecutionResponse.builder()
                .executionId(result.getId())
                .status(result.getStatus().name())
                .output(result.getOutput())
                .error(result.getError())
                .exitCode(result.getExitCode())
                .executionTimeMs(result.getExecutionTimeMs())
                .build();
    }

    @Override
    public String getStatus(UUID executionId) {
        return executionResultRepository.findById(executionId)
                .map(r -> r.getStatus().name())
                .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));
    }

    public record ExecutionPayload(UUID executionId, ExecutionRequest request) {}
}
