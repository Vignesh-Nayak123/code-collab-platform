package com.cce.codeeditor.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.cce.codeeditor.websocket.dto.UserJoinedMessage;
import com.cce.codeeditor.websocket.dto.UserLeftMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private static final String ACTIVE_USERS_PREFIX = "users:";

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomCode = (String) headerAccessor.getSessionAttributes().get("roomCode");

        if (username != null && roomCode != null) {
            addUserToRoom(roomCode, username);
            List<String> activeUsers = getActiveUsers(roomCode);
            String color = generateColor(username);

            UserJoinedMessage message = new UserJoinedMessage(roomCode, username, color, activeUsers);
            messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/user-joined", message);
            log.info("User {} joined room {}", username, roomCode);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomCode = (String) headerAccessor.getSessionAttributes().get("roomCode");

        if (username != null && roomCode != null) {
            removeUserFromRoom(roomCode, username);
            List<String> activeUsers = getActiveUsers(roomCode);

            UserLeftMessage message = new UserLeftMessage(roomCode, username, activeUsers);
            messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/user-left", message);
            log.info("User {} left room {}", username, roomCode);
        }
    }

    private void addUserToRoom(String roomCode, String username) {
        String key = ACTIVE_USERS_PREFIX + roomCode;
        redisTemplate.opsForSet().add(key, username);
    }

    private void removeUserFromRoom(String roomCode, String username) {
        String key = ACTIVE_USERS_PREFIX + roomCode;
        redisTemplate.opsForSet().remove(key, username);
    }

    private List<String> getActiveUsers(String roomCode) {
        String key = ACTIVE_USERS_PREFIX + roomCode;
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) return new ArrayList<>();
        return members.stream().map(Object::toString).toList();
    }

    private String generateColor(String username) {
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
                           "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"};
        int index = Math.abs(username.hashCode()) % colors.length;
        return colors[index];
    }
}
