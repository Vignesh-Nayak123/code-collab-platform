package com.cce.codeeditor.service;

import java.util.UUID;

import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.dto.ExecutionResponse;

public interface CodeExecutionService {
    ExecutionResponse submitExecution(ExecutionRequest request);
    ExecutionResponse getResult(UUID executionId);
    String getStatus(UUID executionId);
}
