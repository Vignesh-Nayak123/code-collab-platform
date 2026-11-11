package com.cce.codeeditor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cce.codeeditor.exception.RoomNotFoundException;
import com.cce.codeeditor.model.dto.CreateRoomRequest;
import com.cce.codeeditor.model.dto.CreateRoomResponse;
import com.cce.codeeditor.model.dto.RoomInfoResponse;
import com.cce.codeeditor.model.entity.Document;
import com.cce.codeeditor.model.entity.Room;
import com.cce.codeeditor.model.enums.RoomStatus;
import com.cce.codeeditor.repository.DocumentRepository;
import com.cce.codeeditor.repository.RoomRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private static final String ROOM_CACHE_PREFIX = "room:";
    private static final String DOC_CONTENT_PREFIX = "doc:";
    private static final String ACTIVE_USERS_PREFIX = "users:";
    private static final Duration ROOM_CACHE_TTL = Duration.ofHours(24);
    private static final Duration DOC_CACHE_TTL = Duration.ofHours(24);
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final RoomRepository roomRepository;
    private final DocumentRepository documentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public CreateRoomResponse createRoom(CreateRoomRequest request) {
        String roomCode = generateUniqueRoomCode();

        Room room = Room.builder()
                .name(request.getName())
                .roomCode(roomCode)
                .language(request.getLanguage())
                .maxUsers(request.getMaxUsers())
                .status(RoomStatus.ACTIVE)
                .createdBy(request.getCreatedBy())
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build();

        room = roomRepository.save(room);

        Document document = Document.builder()
                .roomId(room.getId())
                .content("")
                .language(request.getLanguage().name().toLowerCase())
                .build();
        documentRepository.save(document);

        cacheRoom(room);

        log.info("Created room: {} with code: {}", room.getName(), roomCode);

        return CreateRoomResponse.builder()
                .id(room.getId())
                .roomCode(roomCode)
                .name(room.getName())
                .language(room.getLanguage().name())
                .joinUrl("/room/" + roomCode)
                .build();
    }

    @Override
    public RoomInfoResponse getRoomInfo(String roomCode) {
        Room room = getRoomByCode(roomCode);
        String content = getDocumentContent(room);
        int activeUsersCount = getActiveUsersCount(roomCode);

        return RoomInfoResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .roomCode(room.getRoomCode())
                .language(room.getLanguage().name())
                .maxUsers(room.getMaxUsers())
                .activeUsersCount(activeUsersCount)
                .status(room.getStatus().name())
                .createdAt(room.getCreatedAt())
                .documentContent(content)
                .build();
    }

    @Override
    public Room getRoomByCode(String roomCode) {
        String cacheKey = ROOM_CACHE_PREFIX + roomCode;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Room) {
            return (Room) cached;
        }

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + roomCode));
        redisTemplate.opsForValue().set(cacheKey, room, ROOM_CACHE_TTL);
        return room;
    }

    @Override
    @Transactional
    public void closeRoom(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + roomCode));
        room.setStatus(RoomStatus.CLOSED);
        roomRepository.save(room);

        String cacheKey = ROOM_CACHE_PREFIX + roomCode;
        redisTemplate.delete(cacheKey);

        log.info("Closed room: {}", roomCode);
    }

    @Override
    public boolean roomExists(String roomCode) {
        return roomRepository.existsByRoomCode(roomCode);
    }

    private void cacheRoom(Room room) {
        String cacheKey = ROOM_CACHE_PREFIX + room.getRoomCode();
        redisTemplate.opsForValue().set(cacheKey, room, ROOM_CACHE_TTL);
    }

    private String getDocumentContent(Room room) {
        String cacheKey = DOC_CONTENT_PREFIX + room.getRoomCode();
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof String) {
            return (String) cached;
        }

        String content = documentRepository.findByRoomId(room.getId())
                .map(Document::getContent)
                .orElse("");
        redisTemplate.opsForValue().set(cacheKey, content, DOC_CACHE_TTL);
        return content;
    }

    private int getActiveUsersCount(String roomCode) {
        String usersKey = ACTIVE_USERS_PREFIX + roomCode;
        Set<Object> members = redisTemplate.opsForSet().members(usersKey);
        return members != null ? members.size() : 0;
    }

    private String generateUniqueRoomCode() {
        SecureRandom random = new SecureRandom();
        String code;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
            }
            code = sb.toString();
        } while (roomRepository.existsByRoomCode(code));
        return code;
    }
}
