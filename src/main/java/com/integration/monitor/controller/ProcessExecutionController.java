package com.integration.monitor.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.integration.monitor.dto.ExecutionSearchRequest;
import com.integration.monitor.dto.FailExecutionRequest;
import com.integration.monitor.dto.ProcessExecutionResponse;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.service.ProcessExecutionService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/processes/{processId}/executions")
@Tag(
        name = "Process Executions",
        description = "Operations for managing process executions"
)
public class ProcessExecutionController {

    private final ProcessExecutionService service;

    public ProcessExecutionController(ProcessExecutionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProcessExecutionResponse> startExecution(
            @PathVariable Long processId) {

        ProcessExecution execution
                = service.startExecution(processId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProcessExecutionResponse.from(execution));
    }

    @PostMapping("/{executionId}/complete")
    public ResponseEntity<ProcessExecutionResponse> completeExecution(
            @PathVariable Long processId,
            @PathVariable Long executionId) {

        ProcessExecution execution
                = service.completeExecution(processId, executionId);

        return ResponseEntity.ok(
                ProcessExecutionResponse.from(execution));
    }

    @PostMapping("/{executionId}/fail")
    public ResponseEntity<ProcessExecutionResponse> failExecution(
            @PathVariable Long processId,
            @PathVariable Long executionId,
            @Valid @RequestBody FailExecutionRequest request) {

        ProcessExecution execution
                = service.failExecution(
                        processId,
                        executionId,
                        request.getErrorMessage());

        return ResponseEntity.ok(
                ProcessExecutionResponse.from(execution));
    }

    @GetMapping
    public ResponseEntity<Page<ProcessExecutionResponse>> getExecutions(
            @PathVariable Long processId,
            ExecutionSearchRequest request,
            Pageable pageable) {

        Page<ProcessExecution> executions
                = service.getExecutions(
                        processId,
                        request,
                        pageable);

        return ResponseEntity.ok(
                executions.map(ProcessExecutionResponse::from));
    }
}
