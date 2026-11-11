package com.cce.codeeditor.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.cce.codeeditor.service.DocumentService;
import com.cce.codeeditor.service.RedisPubSubService;
import com.cce.codeeditor.websocket.dto.ChatMessage;
import com.cce.codeeditor.websocket.dto.CodeChangeMessage;
import com.cce.codeeditor.websocket.dto.CursorPositionMessage;

import java.time.Instant;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EditorWebSocketHandler {

    private final DocumentService documentService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisPubSubService redisPubSubService;

    @MessageMapping("/editor/{roomCode}/code-change")
    public void handleCodeChange(@DestinationVariable String roomCode,
                                  CodeChangeMessage message) {
        documentService.updateContent(roomCode, message.getContent(), message.getVersion());
        redisPubSubService.publish("code-change:" + roomCode, message);
        log.debug("Code change in room: {} by user: {}", roomCode, message.getUsername());
    }

    @MessageMapping("/editor/{roomCode}/cursor")
    public void handleCursorMove(@DestinationVariable String roomCode,
                                  CursorPositionMessage message) {
        messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/cursor", message);
    }

    @MessageMapping("/editor/{roomCode}/chat")
    public void handleChat(@DestinationVariable String roomCode,
                           ChatMessage message) {
        message.setTimestamp(Instant.now().toString());
        messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/chat", message);
        log.debug("Chat message in room: {} from: {}", roomCode, message.getUsername());
    }
}
