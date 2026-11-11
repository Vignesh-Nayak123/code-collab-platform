package com.cce.codeeditor.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String roomCode;
    private String username;
    private String message;
    private String timestamp;
}
