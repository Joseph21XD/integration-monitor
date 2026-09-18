package com.integration.monitor.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ProcessStatus",
        description = "Current status of an integration process"
)
public enum ProcessStatus {

    @Schema(description = "Process is currently running")
    RUNNING,

    @Schema(description = "Process completed successfully")
    SUCCESS,

    @Schema(description = "Process execution failed")
    FAILED,

    @Schema(description = "Process was manually stopped")
    STOPPED
}

