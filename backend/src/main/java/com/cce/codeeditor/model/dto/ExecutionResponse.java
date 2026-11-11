package com.cce.codeeditor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {
    private UUID executionId;
    private String status;
    private String output;
    private String error;
    private Integer exitCode;
    private Long executionTimeMs;
}
