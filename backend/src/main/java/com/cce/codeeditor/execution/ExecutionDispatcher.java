package com.cce.codeeditor.execution;

import com.cce.codeeditor.model.dto.ExecutionRequest;
import com.cce.codeeditor.model.entity.ExecutionResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Hands a freshly saved PENDING {@link ExecutionResult} to whatever runs it.
 * Exactly one implementation is active, selected by {@code app.execution.mode}:
 * <ul>
 *   <li>{@code kafka} (default): publish to the execution-requests topic</li>
 *   <li>{@code inline}: run in-process on a bounded worker (Docker still required)</li>
 *   <li>{@code disabled}: immediately mark the run FAILED with an explanatory message</li>
 * </ul>
 * Called inside the submitting transaction.
 */
public interface ExecutionDispatcher {

    void dispatch(ExecutionResult result, ExecutionRequest request);

    /**
     * Runs {@code task} once the current transaction has committed (immediately if
     * there is none), so a worker never looks up an execution row that isn't
     * visible yet. The task must not use the JPA/JDBC resources of the finished
     * transaction: hand work to another thread or to an external system.
     */
    static void afterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
