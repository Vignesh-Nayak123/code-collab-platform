package com.cce.codeeditor.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeChangeMessage {
    private String roomCode;
    private String username;
    private String content;
    private Long version;
    private String timestamp;
}
