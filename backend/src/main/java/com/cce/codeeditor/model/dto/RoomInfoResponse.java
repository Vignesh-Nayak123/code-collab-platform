package com.cce.codeeditor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoResponse {
    private UUID id;
    private String name;
    private String roomCode;
    private String language;
    private Integer maxUsers;
    private Integer activeUsersCount;
    private String status;
    private Instant createdAt;
    private String documentContent;
}
