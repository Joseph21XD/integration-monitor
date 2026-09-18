package com.integration.monitor.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.integration.monitor.dto.ProcessExecutionResponse;
import com.integration.monitor.exception.ExecutionNotFoundException;
import com.integration.monitor.exception.InvalidStatusTransitionException;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.service.ProcessExecutionService;

@WebMvcTest(ProcessExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProcessExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessExecutionService service;

    @Test
    void shouldStartExecution() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.RUNNING);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.of(2026, 9, 12, 10, 0),
                        ProcessStatus.RUNNING);

        ProcessExecutionResponse response
                = ProcessExecutionResponse.from(execution);

        when(service.startExecution(1L))
                .thenReturn(execution);

        mockMvc.perform(
                post("/api/processes/1/executions"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status")
                        .value("RUNNING"));

        verify(service).startExecution(1L);
    }

    @Test
    void shouldCompleteExecution() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.SUCCESS);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.of(2026, 9, 12, 10, 0),
                        ProcessStatus.SUCCESS);

        execution.finish(ProcessStatus.SUCCESS);

        when(service.completeExecution(1L, 10L))
                .thenReturn(execution);

        mockMvc.perform(
                post("/api/processes/1/executions/10/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("SUCCESS"));

        verify(service).completeExecution(1L, 10L);
    }

    @Test
    void shouldFailExecution() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.FAILED);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.of(2026, 9, 12, 10, 0),
                        ProcessStatus.FAILED);

        execution.fail("Connection timeout");

        when(service.failExecution(
                1L,
                10L,
                "Connection timeout"))
                .thenReturn(execution);

        mockMvc.perform(
                post("/api/processes/1/executions/10/fail")
                        .param("errorMessage", "Connection timeout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("FAILED"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Connection timeout"));

        verify(service).failExecution(
                1L,
                10L,
                "Connection timeout");
    }

    @Test
    void shouldGetExecutions() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.SUCCESS);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.of(2026, 9, 12, 10, 0),
                        ProcessStatus.SUCCESS);

        Page<ProcessExecution> page
                = new PageImpl<>(List.of(execution));

        when(service.getExecutions(
                eq(1L),
                isNull(),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/processes/1/executions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status")
                        .value("SUCCESS"));

        verify(service).getExecutions(
                eq(1L),
                argThat(request
                        -> request.getStatus() == null
                && request.getFrom() == null
                && request.getTo() == null),
                any(Pageable.class));
    }

    @Test
    void shouldReturnNotFoundWhenExecutionDoesNotExist() throws Exception {

        when(service.completeExecution(1L, 999L))
                .thenThrow(new ExecutionNotFoundException(999L));

        mockMvc.perform(
                post("/api/processes/1/executions/999/complete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Execution not found: 999"));
    }

    @Test
    void shouldReturnBadRequestForInvalidExecutionTransition()
            throws Exception {

        when(service.completeExecution(1L, 10L))
                .thenThrow(
                        new InvalidStatusTransitionException(
                                "Execution is already finished with status: SUCCESS"));

        mockMvc.perform(
                post("/api/processes/1/executions/10/complete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Execution is already finished with status: SUCCESS"));
    }

    @Test
    void shouldRejectFailExecutionWithoutErrorMessage()
            throws Exception {

        mockMvc.perform(
                post("/api/processes/1/executions/10/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "errorMessage": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectFailExecutionWithErrorMessageTooLong()
            throws Exception {

        String errorMessage = "a".repeat(1001);

        mockMvc.perform(
                post("/api/processes/1/executions/10/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "errorMessage": "%s"
                        }
                        """.formatted(errorMessage)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldGetExecutionsFilteredByStatus() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.FAILED);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.of(2026, 9, 12, 10, 0),
                        ProcessStatus.FAILED);

        execution.fail("Connection timeout");

        Page<ProcessExecution> page
                = new PageImpl<>(List.of(execution));

        when(service.getExecutions(
                eq(1L),
                argThat(request
                        -> request.getStatus() == null
                && request.getFrom() == null
                && request.getTo() == null),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/processes/1/executions")
                        .param("status", "FAILED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status")
                        .value("FAILED"));

        verify(service).getExecutions(
                eq(1L),
                argThat(request
                        -> request.getStatus() == null
                && request.getFrom() == null
                && request.getTo() == null),
                any(Pageable.class));
    }
}
