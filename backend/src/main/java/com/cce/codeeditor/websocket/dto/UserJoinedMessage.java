package com.cce.codeeditor.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinedMessage {
    private String roomCode;
    private String username;
    private String color;
    private List<String> activeUsers;
}
