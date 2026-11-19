package com.cce.codeeditor.execution;

import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DisabledExecutionDispatcherTest {

    @Test
    void dispatch_marksResultFailedWithMessageWithoutRunningCode() {
        DisabledExecutionDispatcher dispatcher = new DisabledExecutionDispatcher("disabled here");
        ExecutionResult result = ExecutionResult.builder()
                .id(UUID.randomUUID())
                .status(ExecutionStatus.PENDING)
                .build();

        dispatcher.dispatch(result, new ExecutionRequest());

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(result.getError()).isEqualTo("disabled here");
        assertThat(result.getExitCode()).isEqualTo(-1);
        assertThat(result.getCompletedAt()).isNotNull();
    }
}
