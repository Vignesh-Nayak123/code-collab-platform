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
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeExecutionServiceTest {

    @Mock
    private ExecutionResultRepository executionResultRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ExecutionRequestProducer executionRequestProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CodeExecutionServiceImpl codeExecutionService;

    @Test
    void submitExecution_shouldCreatePendingResult() throws Exception {
        ExecutionRequest request = new ExecutionRequest();
        request.setRoomCode("TESTROOM");
        request.setLanguage("PYTHON");
        request.setCode("print('hello')");
        request.setSubmittedBy("user1");

        Room room = Room.builder().id(UUID.randomUUID()).roomCode("TESTROOM").build();
        ExecutionResult savedResult = ExecutionResult.builder()
                .id(UUID.randomUUID())
                .status(ExecutionStatus.PENDING)
                .build();

        when(roomRepository.findByRoomCode("TESTROOM")).thenReturn(Optional.of(room));
        when(executionResultRepository.save(any(ExecutionResult.class))).thenReturn(savedResult);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(executionRequestProducer).sendExecutionRequest(anyString(), anyString());

        ExecutionResponse response = codeExecutionService.submitExecution(request);

        assertThat(response).isNotNull();
        assertThat(response.getExecutionId()).isEqualTo(savedResult.getId());
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(executionResultRepository).save(any(ExecutionResult.class));
        verify(executionRequestProducer).sendExecutionRequest(anyString(), anyString());
    }

    @Test
    void submitExecution_shouldThrowWhenRoomNotFound() {
        ExecutionRequest request = new ExecutionRequest();
        request.setRoomCode("NOTFOUND");
        request.setLanguage("PYTHON");
        request.setCode("print('hello')");

        when(roomRepository.findByRoomCode("NOTFOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> codeExecutionService.submitExecution(request))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void getResult_shouldReturnResultWhenFound() {
        UUID id = UUID.randomUUID();
        ExecutionResult result = ExecutionResult.builder()
                .id(id)
                .status(ExecutionStatus.COMPLETED)
                .output("Hello World\n")
                .exitCode(0)
                .build();

        when(executionResultRepository.findById(id)).thenReturn(Optional.of(result));

        ExecutionResponse response = codeExecutionService.getResult(id);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getOutput()).isEqualTo("Hello World\n");
    }
}
