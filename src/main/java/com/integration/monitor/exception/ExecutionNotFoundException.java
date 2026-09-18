package com.integration.monitor.exception;

public class ExecutionNotFoundException extends RuntimeException {

    public ExecutionNotFoundException(Long executionId) {
        super("Execution not found: " + executionId);
    }
}
