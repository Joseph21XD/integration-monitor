package com.integration.monitor.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.integration.monitor.model.ProcessStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public class ExecutionSearchRequest {

    @Schema(
            description = "Execution status",
            example = "FAILED"
    )
    private ProcessStatus status;

    @Schema(
            description = "Start of the search range",
            example = "2026-09-12T00:00:00"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @Schema(
            description = "End of the search range",
            example = "2026-09-12T23:59:59"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }
}
