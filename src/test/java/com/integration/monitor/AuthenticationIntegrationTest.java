package com.integration.monitor;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.integration.monitor.service.IntegrationProcessService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntegrationProcessService service;

    @Test
    void shouldLoginAndAccessProtectedEndpoint()
            throws Exception {

        String loginRequest =
                """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;

        String token =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token").exists()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String jwt =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(token)
                        .get("token")
                        .asText();

        when(service.getAllProcesses(any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(),
                                PageRequest.of(0, 10),
                                0
                        )
                );

        mockMvc.perform(
                get("/api/processes")
                        .header(
                                "Authorization",
                                "Bearer " + jwt
                        )
        )
                .andExpect(status().isOk());
    }
}

