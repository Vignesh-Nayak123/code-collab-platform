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
public class CreateRoomResponse {
    private UUID id;
    private String roomCode;
    private String name;
    private String language;
    private String joinUrl;
}
