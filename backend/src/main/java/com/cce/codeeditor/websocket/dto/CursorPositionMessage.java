package com.cce.codeeditor.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPositionMessage {
    private String roomCode;
    private String username;
    private Integer lineNumber;
    private Integer column;
    private Integer selectionStartLine;
    private Integer selectionStartColumn;
    private Integer selectionEndLine;
    private Integer selectionEndColumn;
    private String color;
}
