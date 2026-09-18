package com.integration.monitor.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.monitor.dto.ChangeStatusRequest;
import com.integration.monitor.dto.CreateProcessRequest;
import com.integration.monitor.exception.GlobalExceptionHandler;
import com.integration.monitor.exception.ProcessNotFoundException;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.security.JwtAuthenticationFilter;
import com.integration.monitor.security.JwtService;
import com.integration.monitor.service.IntegrationProcessService;

@ActiveProfiles("dev")
@WebMvcTest(IntegrationProcessController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class IntegrationProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IntegrationProcessService service;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnProcesses() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING
                );

        PageImpl<IntegrationProcess> page
                = new PageImpl<>(
                        List.of(process),
                        PageRequest.of(0, 10),
                        1
                );

        when(service.getAllProcesses(any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/processes")
                        .param("page", "0")
                        .param("size", "10")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("SINPE Service"))
                .andExpect(jsonPath("$.content[0].type")
                        .value("OSB"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("RUNNING"));

        verify(service).getAllProcesses(any());
    }

    @Test
    void shouldCreateProcess() throws Exception {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING
                );

        when(service.registerProcess(
                any(IntegrationProcess.class)))
                .thenReturn(process);

        CreateProcessRequest request
                = new CreateProcessRequest();

        request.setName("SINPE Service");
        request.setType(ProcessType.OSB);
        request.setStatus(ProcessStatus.RUNNING);

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("SINPE Service"))
                .andExpect(jsonPath("$.type")
                        .value("OSB"))
                .andExpect(jsonPath("$.status")
                        .value("RUNNING"));

        verify(service)
                .registerProcess(any(IntegrationProcess.class));
    }

    @Test
    void shouldRejectInvalidProcess() throws Exception {

        CreateProcessRequest request
                = new CreateProcessRequest();

        request.setName("");
        request.setType(ProcessType.OSB);
        request.setStatus(ProcessStatus.RUNNING);

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectMissingType() throws Exception {

        CreateProcessRequest request
                = new CreateProcessRequest();

        request.setName("SINPE Service");
        request.setType(null);
        request.setStatus(ProcessStatus.RUNNING);

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectMissingStatus() throws Exception {

        CreateProcessRequest request
                = new CreateProcessRequest();

        request.setName("SINPE Service");
        request.setType(ProcessType.OSB);
        request.setStatus(null);

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldGetProcessById() throws Exception {

        Long id = 1L;

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING
                );

        when(service.getProcessById(id))
                .thenReturn(java.util.Optional.of(process));

        mockMvc.perform(
                get("/api/processes/{id}", id)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("SINPE Service"))
                .andExpect(jsonPath("$.type")
                        .value("OSB"))
                .andExpect(jsonPath("$.status")
                        .value("RUNNING"));

        verify(service).getProcessById(id);
    }

    @Test
    void shouldReturn404WhenProcessDoesNotExist()
            throws Exception {

        Long id = 999L;

        when(service.getProcessById(id))
                .thenThrow(
                        new ProcessNotFoundException(id)
                );

        mockMvc.perform(
                get("/api/processes/{id}", id)
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Integration process not found with ID: 999"
                        ));

        verify(service).getProcessById(id);
    }

    @Test
    void shouldChangeStatus() throws Exception {

        Long id = 1L;

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS
                );

        ChangeStatusRequest request
                = new ChangeStatusRequest();

        request.setStatus(ProcessStatus.SUCCESS);

        when(service.changeStatus(
                eq(id),
                eq(ProcessStatus.SUCCESS)
        )).thenReturn(process);

        mockMvc.perform(
                put("/api/processes/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("SUCCESS"));

        verify(service)
                .changeStatus(id, ProcessStatus.SUCCESS);
    }

    @Test
    void shouldRejectInvalidStatusRequest()
            throws Exception {

        ChangeStatusRequest request
                = new ChangeStatusRequest();

        request.setStatus(null);

        mockMvc.perform(
                put("/api/processes/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectInvalidJson() throws Exception {

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "SINPE Service",
                                "type": "INVALID",
                                "status": "RUNNING"
                            }
                            """)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectBlankProcessName() throws Exception {

        String json = """
        {
            "name": "",
            "type": "OSB",
            "status": "RUNNING"
        }
        """;

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectShortProcessName() throws Exception {

        String json = """
        {
            "name": "AB",
            "type": "OSB",
            "status": "RUNNING"
        }
        """;

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectNullProcessType() throws Exception {

        String json = """
        {
            "name": "SINPE Service",
            "type": null,
            "status": "RUNNING"
        }
        """;

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectNullProcessStatus() throws Exception {

        String json = """
        {
            "name": "SINPE Service",
            "type": "OSB",
            "status": null
        }
        """;

        mockMvc.perform(
                post("/api/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn404WhenChangingStatusOfNonExistingProcess()
            throws Exception {

        Long processId = 999L;

        when(service.changeStatus(
                eq(processId),
                eq(ProcessStatus.SUCCESS)
        )).thenThrow(
                new ProcessNotFoundException(processId)
        );

        String json = """
        {
            "status": "SUCCESS"
        }
        """;

        mockMvc.perform(
                put("/api/processes/{id}/status", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isNotFound());

        verify(service)
                .changeStatus(processId, ProcessStatus.SUCCESS);
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {

        when(service.getProcessById(1L))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(
                get("/api/processes/1")
        )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(
                        "An unexpected error occurred"
                ));

        verify(service).getProcessById(1L);
    }

}
