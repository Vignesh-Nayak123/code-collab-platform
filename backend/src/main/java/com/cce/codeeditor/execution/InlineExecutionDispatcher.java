package com.cce.codeeditor.execution;

import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.entity.ExecutionResult;
import com.cce.codeeditor.model.enums.ExecutionStatus;
import com.cce.codeeditor.repository.ExecutionResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Kafka-free execution for a single backend instance: runs submissions one at a
 * time on an in-process worker with a small bounded queue. It still uses
 * {@link DockerExecutionService}, so the host needs a Docker daemon. Platforms
 * without one (e.g. Render) should use {@code app.execution.mode=disabled}.
 */
@Component
@ConditionalOnProperty(name = "app.execution.mode", havingValue = "inline")
@Slf4j
public class InlineExecutionDispatcher implements ExecutionDispatcher, DisposableBean {

    private static final int QUEUE_CAPACITY = 20;

    private final ExecutionResultRepository executionResultRepository;
    private final DockerExecutionService dockerExecutionService;
    private final TransactionTemplate newTransaction;
    private final ThreadPoolExecutor worker = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            runnable -> {
                Thread thread = new Thread(runnable, "inline-execution");
                thread.setDaemon(true);
                return thread;
            });

    public InlineExecutionDispatcher(ExecutionResultRepository executionResultRepository,
                                     DockerExecutionService dockerExecutionService,
                                     PlatformTransactionManager transactionManager) {
        this.executionResultRepository = executionResultRepository;
        this.dockerExecutionService = dockerExecutionService;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void dispatch(ExecutionResult result, ExecutionRequest request) {
        UUID executionId = result.getId();
        String code = request.getCode();
        String language = request.getLanguage();
        ExecutionDispatcher.afterCommit(() -> enqueue(executionId, code, language));
    }

    private void enqueue(UUID executionId, String code, String language) {
        try {
            worker.execute(() -> run(executionId, code, language));
        } catch (RejectedExecutionException e) {
            log.warn("Execution queue full, rejecting {}", executionId);
            // Still inside the submitter's afterCommit callback: data access there would join the
            // already-committed transaction and never be written, so use a fresh one.
            newTransaction.executeWithoutResult(status ->
                    executionResultRepository.findById(executionId).ifPresent(r -> {
                        r.setStatus(ExecutionStatus.FAILED);
                        r.setError("Server is busy running other code. Please try again in a few seconds.");
                        r.setExitCode(-1);
                        r.setCompletedAt(Instant.now());
                    }));
        }
    }

    private void run(UUID executionId, String code, String language) {
        try {
            executionResultRepository.findById(executionId).ifPresentOrElse(result -> {
                result.setStatus(ExecutionStatus.RUNNING);
                ExecutionResult running = executionResultRepository.save(result);
                dockerExecutionService.execute(running, code, language);
            }, () -> log.warn("Execution result not found: {}", executionId));
        } catch (Exception e) {
            log.error("Inline execution failed for {}", executionId, e);
        }
    }

    @Override
    public void destroy() {
        worker.shutdownNow();
    }
}
