package com.cce.codeeditor.exception;

public class ExecutionTimeoutException extends RuntimeException {
    public ExecutionTimeoutException(String message) {
        super(message);
    }
}
