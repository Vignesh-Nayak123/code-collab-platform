package com.cce.codeeditor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cce.codeeditor.model.dto.CreateRoomRequest;
import com.cce.codeeditor.model.dto.CreateRoomResponse;
import com.cce.codeeditor.model.dto.RoomInfoResponse;
import com.cce.codeeditor.service.DocumentService;
import com.cce.codeeditor.service.RoomService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management endpoints")
public class RoomController {

    private final RoomService roomService;
    private final DocumentService documentService;

    @PostMapping
    @Operation(summary = "Create a new room")
    public ResponseEntity<CreateRoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping("/{roomCode}")
    @Operation(summary = "Get room information")
    public ResponseEntity<RoomInfoResponse> getRoomInfo(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.getRoomInfo(roomCode));
    }

    @DeleteMapping("/{roomCode}")
    @Operation(summary = "Close a room")
    public ResponseEntity<Void> closeRoom(@PathVariable String roomCode) {
        roomService.closeRoom(roomCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomCode}/document")
    @Operation(summary = "Get current document content")
    public ResponseEntity<Map<String, String>> getDocument(@PathVariable String roomCode) {
        String content = documentService.getContent(roomCode);
        return ResponseEntity.ok(Map.of("content", content));
    }
}
