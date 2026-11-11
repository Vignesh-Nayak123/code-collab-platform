package com.cce.codeeditor.model.dto;

import com.cce.codeeditor.model.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoomRequest {

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String name;

    private Language language = Language.JAVASCRIPT;

    private Integer maxUsers = 10;

    private String createdBy;
}
