package com.integration.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.repository.IntegrationProcessRepository;
import com.integration.monitor.service.IntegrationProcessService;

@ActiveProfiles("dev")
@SpringBootTest
@Transactional
class IntegrationProcessIntegrationTest {

@Autowired
private IntegrationProcessService service;

@Autowired
private IntegrationProcessRepository repository;

@Test
void shouldCreateProcessAndPersistIt() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "Integration Test",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    IntegrationProcess saved =
            service.registerProcess(process);

    assertNotNull(saved);
    assertNotNull(saved.getId());

    IntegrationProcess fromDatabase =
            repository.findById(saved.getId())
                    .orElseThrow();

    assertEquals(
            saved.getId(),
            fromDatabase.getId()
    );

    assertEquals(
            "Integration Test",
            fromDatabase.getName()
    );

    assertEquals(
            ProcessType.OSB,
            fromDatabase.getType()
    );

    assertEquals(
            ProcessStatus.RUNNING,
            fromDatabase.getStatus()
    );
}

@Test
void shouldChangeProcessStatus() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "Status Test",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    IntegrationProcess saved =
            service.registerProcess(process);

    IntegrationProcess updated =
            service.changeStatus(
                    saved.getId(),
                    ProcessStatus.SUCCESS
            );

    assertEquals(
            ProcessStatus.SUCCESS,
            updated.getStatus()
    );

    IntegrationProcess fromDatabase =
            repository.findById(saved.getId())
                    .orElseThrow();

    assertEquals(
            ProcessStatus.SUCCESS,
            fromDatabase.getStatus()
    );
}

}
