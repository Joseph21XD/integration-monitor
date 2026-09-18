package com.integration.monitor.exception;

public class ExecutionProcessMismatchException extends RuntimeException {

    public ExecutionProcessMismatchException(
            Long executionId,
            Long processId) {

        super(
                "Execution " + executionId +
                " does not belong to process " + processId);
    }
}