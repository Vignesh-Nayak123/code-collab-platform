package com.cce.codeeditor.controller;

import com.cce.codeeditor.controller.ExecutionController;
import com.cce.codeeditor.exception.GlobalExceptionHandler;
import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.dto.ExecutionResponse;
import com.cce.codeeditor.service.CodeExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExecutionController.class)
@Import(GlobalExceptionHandler.class)
class ExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CodeExecutionService codeExecutionService;

    @Test
    void submitExecution_shouldReturn202() throws Exception {
        ExecutionRequest request = new ExecutionRequest();
        request.setRoomCode("TEST1234");
        request.setLanguage("PYTHON");
        request.setCode("print('hello')");

        ExecutionResponse response = ExecutionResponse.builder()
                .executionId(UUID.randomUUID())
                .status("PENDING")
                .build();

        when(codeExecutionService.submitExecution(any(ExecutionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getResult_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        ExecutionResponse response = ExecutionResponse.builder()
                .executionId(id)
                .status("COMPLETED")
                .output("Hello World\n")
                .exitCode(0)
                .build();

        when(codeExecutionService.getResult(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/execute/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
