package com.integration.monitor.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.integration.monitor.dto.ApiErrorResponse;
import com.integration.monitor.dto.ChangeStatusRequest;
import com.integration.monitor.dto.CreateProcessRequest;
import com.integration.monitor.dto.IntegrationProcessResponse;
import com.integration.monitor.exception.ProcessNotFoundException;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.service.IntegrationProcessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
        name = "Integration Processes",
        description = "Operations for managing integration processes"
)
@RestController
@RequestMapping("/api/processes")
public class IntegrationProcessController {

    private final IntegrationProcessService service;
    private static final Logger log =
        LoggerFactory.getLogger(IntegrationProcessController.class);

    public IntegrationProcessController(
            IntegrationProcessService service) {

        this.service = service;
    }

    @Operation(
            summary = "Get all integration processes",
            description = "Returns a paginated list of integration processes"
    )
    @GetMapping
    public ResponseEntity<Page<IntegrationProcessResponse>> getProcesses(
            Pageable pageable) {

        log.debug("Request received to get all processes with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<IntegrationProcess> processes = service.getAllProcesses(pageable);

        Page<IntegrationProcessResponse> response
                = processes.map(IntegrationProcessResponse::from);

        log.debug(
                "Returning {} processes for page {}",
                response.getNumberOfElements(),
                response.getNumber()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get an integration process",
            description = "Returns an integration process by its ID"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Process found"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Process not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiErrorResponse.class
                        )
                )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<IntegrationProcessResponse> getProcess(
            @PathVariable Long id) {

        log.debug("Request received to get process: id={}", id);

        IntegrationProcess process
                = service.getProcessById(id)
                        .orElseThrow(
                                () -> new ProcessNotFoundException(id)
                        );

        return ResponseEntity.ok(
                IntegrationProcessResponse.from(process)
        );
    }

    @Operation(
            summary = "Create an integration process",
            description = "Creates a new integration process"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Process created successfully",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = IntegrationProcessResponse.class
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Process not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiErrorResponse.class
                        )
                )
        )
    })
    @PostMapping
    public ResponseEntity<IntegrationProcess> createProcess(
            @Valid @RequestBody CreateProcessRequest request) {

        log.debug("Request received to create process: {}", request);

        IntegrationProcess process
                = new IntegrationProcess(
                        request.getName(),
                        request.getType(),
                        request.getStatus()
                );

        IntegrationProcess created
                = service.registerProcess(process);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @Operation(
            summary = "Change process status",
            description = "Changes the status of an integration process"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Status changed successfully"
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid status transition"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Process not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiErrorResponse.class
                        )
                )
        )
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<IntegrationProcess> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {

        log.debug("Request received to change status for process: id={}, status={}", id, request.getStatus());

        IntegrationProcess updated
                = service.changeStatus(id, request.getStatus());

        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete an integration process",
            description = "Deletes an integration process by its ID"
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "204",
                description = "Process deleted successfully"
        ),
        @ApiResponse(
                responseCode = "404",
                description = "Process not found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(
                                implementation = ApiErrorResponse.class
                        )
                )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcess(
            @PathVariable Long id) {

        log.debug("Request received to delete process: id={}", id);

        service.deleteProcess(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<IntegrationProcessResponse>> search(
            @RequestParam(required = false) ProcessStatus status,
            @RequestParam(required = false) ProcessType type,
            @RequestParam(required = false) String name) {

        log.debug("Request received to search processes: status={}, type={}, name={}", status, type, name);

        List<IntegrationProcessResponse> response
                = service.search(status, type, name)
                        .stream()
                        .map(IntegrationProcessResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

}
