package com.cce.codeeditor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import com.cce.codeeditor.exception.RoomNotFoundException;
import com.cce.codeeditor.model.dto.CreateRoomRequest;
import com.cce.codeeditor.model.dto.CreateRoomResponse;
import com.cce.codeeditor.model.entity.Document;
import com.cce.codeeditor.model.entity.Room;
import com.cce.codeeditor.model.enums.Language;
import com.cce.codeeditor.model.enums.RoomStatus;
import com.cce.codeeditor.repository.DocumentRepository;
import com.cce.codeeditor.repository.RoomRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RoomServiceImpl roomService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void createRoom_shouldReturnRoomWithCode() {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("Test Room");
        request.setLanguage(Language.JAVASCRIPT);
        request.setMaxUsers(10);

        Room savedRoom = Room.builder()
                .id(UUID.randomUUID())
                .name("Test Room")
                .roomCode("ABCD1234")
                .language(Language.JAVASCRIPT)
                .status(RoomStatus.ACTIVE)
                .build();

        when(roomRepository.existsByRoomCode(anyString())).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(documentRepository.save(any(Document.class))).thenReturn(new Document());
        doNothing().when(valueOperations).set(anyString(), any(), any());

        CreateRoomResponse response = roomService.createRoom(request);

        assertThat(response).isNotNull();
        assertThat(response.getRoomCode()).isNotBlank();
        assertThat(response.getName()).isEqualTo("Test Room");
        verify(roomRepository).save(any(Room.class));
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void getRoomByCode_shouldThrowWhenNotFound() {
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);
        when(roomRepository.findByRoomCode("NOTEXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomByCode("NOTEXIST"))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessageContaining("NOTEXIST");
    }

    @Test
    void closeRoom_shouldUpdateStatusAndClearCache() {
        Room room = Room.builder()
                .id(UUID.randomUUID())
                .roomCode("TEST1234")
                .status(RoomStatus.ACTIVE)
                .build();

        when(roomRepository.findByRoomCode("TEST1234")).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        roomService.closeRoom("TEST1234");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.CLOSED);
        verify(roomRepository).save(room);
        verify(redisTemplate).delete("room:TEST1234");
    }
}
