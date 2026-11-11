package com.cce.codeeditor.controller;

import com.cce.codeeditor.controller.RoomController;
import com.cce.codeeditor.exception.GlobalExceptionHandler;
import com.cce.codeeditor.exception.RoomNotFoundException;
import com.cce.codeeditor.model.dto.CreateRoomRequest;
import com.cce.codeeditor.model.dto.CreateRoomResponse;
import com.cce.codeeditor.model.dto.RoomInfoResponse;
import com.cce.codeeditor.model.enums.Language;
import com.cce.codeeditor.service.DocumentService;
import com.cce.codeeditor.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@Import(GlobalExceptionHandler.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private DocumentService documentService;

    @Test
    void createRoom_shouldReturn201() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("My Room");
        request.setLanguage(Language.PYTHON);

        CreateRoomResponse response = CreateRoomResponse.builder()
                .id(UUID.randomUUID())
                .roomCode("ABCD1234")
                .name("My Room")
                .language("PYTHON")
                .joinUrl("/room/ABCD1234")
                .build();

        when(roomService.createRoom(any(CreateRoomRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomCode").value("ABCD1234"))
                .andExpect(jsonPath("$.name").value("My Room"));
    }

    @Test
    void createRoom_shouldReturn400WhenNameBlank() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest();
        request.setName("");

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRoom_shouldReturn200() throws Exception {
        RoomInfoResponse info = RoomInfoResponse.builder()
                .roomCode("ABCD1234")
                .name("My Room")
                .language("PYTHON")
                .build();

        when(roomService.getRoomInfo("ABCD1234")).thenReturn(info);

        mockMvc.perform(get("/api/v1/rooms/ABCD1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value("ABCD1234"));
    }

    @Test
    void getRoom_shouldReturn404WhenNotFound() throws Exception {
        when(roomService.getRoomInfo("NOTFOUND"))
                .thenThrow(new RoomNotFoundException("Room not found: NOTFOUND"));

        mockMvc.perform(get("/api/v1/rooms/NOTFOUND"))
                .andExpect(status().isNotFound());
    }
}
