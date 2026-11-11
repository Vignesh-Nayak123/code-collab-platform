package com.cce.codeeditor.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import com.cce.codeeditor.model.enums.Language;
import com.cce.codeeditor.model.enums.RoomStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "room_code", unique = true, nullable = false, length = 10)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20)
    @Builder.Default
    private Language language = Language.JAVASCRIPT;

    @Column(name = "max_users")
    @Builder.Default
    private Integer maxUsers = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
