package com.cce.codeeditor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cce.codeeditor.model.entity.Room;
import com.cce.codeeditor.model.enums.RoomStatus;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);
    long countByStatus(RoomStatus status);
}
