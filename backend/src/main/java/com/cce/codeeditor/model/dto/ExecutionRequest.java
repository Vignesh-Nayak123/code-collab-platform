package com.cce.codeeditor.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecutionRequest {

    @NotBlank(message = "Room code is required")
    private String roomCode;

    @NotNull(message = "Language is required")
    private String language;

    @NotBlank(message = "Code is required")
    private String code;

    private String submittedBy;
}
