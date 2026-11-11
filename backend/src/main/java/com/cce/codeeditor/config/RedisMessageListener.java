package com.cce.codeeditor.config;

import com.cce.codeeditor.websocket.dto.CodeChangeMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageListener implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode payloadNode = objectMapper.readTree(payload);
            if (payloadNode.isTextual()) {
                payloadNode = objectMapper.readTree(payloadNode.asText());
            }
            CodeChangeMessage codeChange = objectMapper.treeToValue(payloadNode, CodeChangeMessage.class);
            String roomCode = codeChange.getRoomCode();
            log.debug("Rebroadcasting code change for room: {} via Redis", roomCode);
            messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/code-change", codeChange);
        } catch (Exception e) {
            log.error("Failed to rebroadcast code change from Redis", e);
        }
    }
}