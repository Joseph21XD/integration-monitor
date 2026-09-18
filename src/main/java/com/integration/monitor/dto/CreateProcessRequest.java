package com.integration.monitor.dto;

import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        name = "CreateProcessRequest",
        description = "Request used to create an integration process"
)
public class CreateProcessRequest {

    @Schema(
            description = "Name of the integration process",
            example = "SINPE Service",
            minLength = 3,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Schema(
            description = "Type of integration process",
            example = "OSB",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private ProcessType type;

    @Schema(
            description = "Initial status of the process",
            example = "RUNNING",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private ProcessStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProcessType getType() {
        return type;
    }

    public void setType(ProcessType type) {
        this.type = type;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }
}
