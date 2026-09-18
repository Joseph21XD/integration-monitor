package com.integration.monitor.dto;

import java.time.LocalDateTime;

import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;

public class ProcessExecutionResponse {

    private Long id;
    private Long processId;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private ProcessStatus status;
    private String errorMessage;

    public ProcessExecutionResponse(
            Long id,
            Long processId,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            ProcessStatus status,
            String errorMessage) {

        this.id = id;
        this.processId = processId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static ProcessExecutionResponse from(
            ProcessExecution execution) {

        return new ProcessExecutionResponse(
                execution.getId(),
                execution.getProcess().getId(),
                execution.getStartedAt(),
                execution.getFinishedAt(),
                execution.getStatus(),
                execution.getErrorMessage()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getProcessId() {
        return processId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}