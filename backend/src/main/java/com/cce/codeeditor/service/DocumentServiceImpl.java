package com.cce.codeeditor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cce.codeeditor.model.entity.Document;
import com.cce.codeeditor.repository.DocumentRepository;
import com.cce.codeeditor.repository.RoomRepository;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private static final String DOC_CONTENT_PREFIX = "doc:";
    private static final Duration DOC_CACHE_TTL = Duration.ofHours(24);

    private final DocumentRepository documentRepository;
    private final RoomRepository roomRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void updateContent(String roomCode, String content, Long version) {
        String key = DOC_CONTENT_PREFIX + roomCode;
        redisTemplate.opsForValue().set(key, content, DOC_CACHE_TTL);
        log.debug("Updated document content in Redis for room: {}", roomCode);
    }

    @Override
    public String getContent(String roomCode) {
        String key = DOC_CONTENT_PREFIX + roomCode;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof String) {
            return (String) cached;
        }

        return roomRepository.findByRoomCode(roomCode)
                .flatMap(room -> documentRepository.findByRoomId(room.getId()))
                .map(Document::getContent)
                .orElse("");
    }

    @Override
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void flushToDatabase() {
        log.debug("Running scheduled document flush to database");
        Set<String> keys = redisTemplate.keys(DOC_CONTENT_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            String roomCode = key.substring(DOC_CONTENT_PREFIX.length());
            Object content = redisTemplate.opsForValue().get(key);
            if (content instanceof String) {
                persistDocument(roomCode, (String) content);
            }
        }
    }

    private void persistDocument(String roomCode, String content) {
        roomRepository.findByRoomCode(roomCode).ifPresent(room -> {
            documentRepository.findByRoomId(room.getId()).ifPresent(doc -> {
                doc.setContent(content);
                documentRepository.save(doc);
                log.debug("Persisted document for room: {}", roomCode);
            });
        });
    }
}
