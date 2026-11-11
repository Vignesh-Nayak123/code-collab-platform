package com.cce.codeeditor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.enums.ExecutionStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionResultRepository extends JpaRepository<ExecutionResult, UUID> {
    List<ExecutionResult> findByRoomIdOrderByCreatedAtDesc(UUID roomId);
    List<ExecutionResult> findByStatus(ExecutionStatus status);
}
