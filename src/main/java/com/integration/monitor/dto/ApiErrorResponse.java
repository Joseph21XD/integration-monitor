package com.integration.monitor.dto;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ApiErrorResponse",
        description = "Standard error response returned by the API"
)
public class ApiErrorResponse {

    @Schema(
            description = "Date and time when the error occurred",
            example = "2026-09-04T16:30:00"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code",
            example = "404"
    )
    private int status;

    @Schema(
            description = "HTTP error description",
            example = "Not Found"
    )
    private String error;

    @Schema(
            description = "Detailed error message",
            example = "Integration process not found with id: 15"
    )
    private String message;

    @Schema(
            description = "Request path that generated the error",
            example = "/api/processes/15"
    )
    private String path;

    @Schema(
            description = "Validation errors by field",
            example = "{\"name\":\"must not be blank\"}"
    )
    private Map<String, String> errors;

    public ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path) {

        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> errors) {

        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

}
