package com.integration.monitor.dto;

import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;

import java.time.LocalDateTime;

public class IntegrationProcessResponse {

    private Long id;
    private String name;
    private ProcessType type;
    private ProcessStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IntegrationProcessResponse(
            Long id,
            String name,
            ProcessType type,
            ProcessStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static IntegrationProcessResponse from(
            IntegrationProcess process) {

        return new IntegrationProcessResponse(
                process.getId(),
                process.getName(),
                process.getType(),
                process.getStatus(),
                process.getCreatedAt(),
                process.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProcessType getType() {
        return type;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}