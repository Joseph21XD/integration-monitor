
package com.integration.monitor.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ProcessType",
        description = "Type of integration process"
)
public enum ProcessType {

    @Schema(description = "Oracle Service Bus integration")
    OSB,

    @Schema(description = "REST API integration")
    REST_API,

    @Schema(description = "Scheduled or automated batch process")
    BATCH,

    @Schema(description = "Database integration process")
    DATABASE,

    @Schema(description = "SOAP web service integration")
    SOAP
}
