package com.cce.codeeditor.service;

import com.cce.codeeditor.model.dto.CreateRoomRequest;
import com.cce.codeeditor.model.dto.CreateRoomResponse;
import com.cce.codeeditor.model.dto.RoomInfoResponse;
import com.cce.codeeditor.model.entity.Room;

public interface RoomService {
    CreateRoomResponse createRoom(CreateRoomRequest request);
    RoomInfoResponse getRoomInfo(String roomCode);
    Room getRoomByCode(String roomCode);
    void closeRoom(String roomCode);
    boolean roomExists(String roomCode);
}
