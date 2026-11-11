package com.cce.codeeditor.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLeftMessage {
    private String roomCode;
    private String username;
    private List<String> activeUsers;
}
