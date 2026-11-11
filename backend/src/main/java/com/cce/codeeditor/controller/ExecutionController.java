package com.cce.codeeditor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.dto.ExecutionResponse;
import com.cce.codeeditor.service.CodeExecutionService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/execute")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "Code execution endpoints")
public class ExecutionController {

    private final CodeExecutionService codeExecutionService;

    @PostMapping
    @Operation(summary = "Submit code for execution")
    public ResponseEntity<ExecutionResponse> submitExecution(@Valid @RequestBody ExecutionRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(codeExecutionService.submitExecution(request));
    }

    @GetMapping("/{executionId}")
    @Operation(summary = "Get execution result")
    public ResponseEntity<ExecutionResponse> getResult(@PathVariable UUID executionId) {
        return ResponseEntity.ok(codeExecutionService.getResult(executionId));
    }

    @GetMapping("/{executionId}/status")
    @Operation(summary = "Get execution status")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable UUID executionId) {
        String status = codeExecutionService.getStatus(executionId);
        return ResponseEntity.ok(Map.of("status", status));
    }
}
