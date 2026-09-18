package com.integration.monitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FailExecutionRequest {

    @NotBlank
    @Size(max = 1000)
    @Schema(
        description = "Error message that caused the execution to fail",
        example = "Connection timeout"
    )
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
