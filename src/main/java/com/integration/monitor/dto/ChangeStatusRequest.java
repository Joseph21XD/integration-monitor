package com.integration.monitor.dto;

import com.integration.monitor.model.ProcessStatus;

import jakarta.validation.constraints.NotNull;


public class ChangeStatusRequest {

    @NotNull
    private ProcessStatus status;

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }
}