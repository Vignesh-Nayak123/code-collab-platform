package com.cce.codeeditor.execution;

import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.enums.ExecutionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Demo/hosted mode: user code is never run. The submission is recorded and
 * immediately completed as FAILED with an explanatory message, which the
 * frontend shows in the output panel.
 * <p>
 * Use this anywhere there is no isolated sandbox (no Docker daemon), such as
 * Render's free tier. Never run user code directly inside the backend
 * container: it would have the backend's environment variables (DB and Redis
 * credentials) and network access.
 */
@Component
@ConditionalOnProperty(name = "app.execution.mode", havingValue = "disabled")
public class DisabledExecutionDispatcher implements ExecutionDispatcher {

    private final String message;

    public DisabledExecutionDispatcher(
            @Value("${app.execution.disabled-message:Code execution is disabled on this demo deployment. "
                    + "Run the project locally with Docker to execute code.}") String message) {
        this.message = message;
    }

    @Override
    public void dispatch(ExecutionResult result, ExecutionRequest request) {
        // Runs inside the submitting transaction: the managed entity is flushed on commit.
        result.setStatus(ExecutionStatus.FAILED);
        result.setError(message);
        result.setExitCode(-1);
        result.setExecutionTimeMs(0L);
        result.setCompletedAt(Instant.now());
    }
}
