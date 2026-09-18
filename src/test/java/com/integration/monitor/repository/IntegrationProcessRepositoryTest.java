package com.integration.monitor.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class IntegrationProcessRepositoryTest {

@Autowired
private IntegrationProcessRepository repository;

@Test
void shouldSaveProcess() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "Repository Test",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    IntegrationProcess saved =
            repository.save(process);

    assertNotNull(saved);
    assertNotNull(saved.getId());
    assertEquals(
            "Repository Test",
            saved.getName()
    );
}

@Test
void shouldFindProcessById() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "SINPE Service",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    IntegrationProcess saved =
            repository.saveAndFlush(process);

    IntegrationProcess result =
            repository.findById(saved.getId())
                    .orElseThrow();

    assertEquals(
            saved.getId(),
            result.getId()
    );

    assertEquals(
            "SINPE Service",
            result.getName()
    );
}

@Test
void shouldFindProcessesByStatus() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "Running Process",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    repository.saveAndFlush(process);

    List<IntegrationProcess> result =
            repository.findByStatus(
                    ProcessStatus.RUNNING
            );

    assertFalse(result.isEmpty());

    assertTrue(
            result.stream()
                    .allMatch(
                            p -> p.getStatus()
                                    == ProcessStatus.RUNNING
                    )
    );
}

@Test
void shouldFindProcessesByType() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "OSB Process",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    repository.saveAndFlush(process);

    List<IntegrationProcess> result =
            repository.findByType(
                    ProcessType.OSB
            );

    assertFalse(result.isEmpty());

    assertTrue(
            result.stream()
                    .allMatch(
                            p -> p.getType()
                                    == ProcessType.OSB
                    )
    );
}

@Test
void shouldSearchByName() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "SINPE Payment Service",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    repository.saveAndFlush(process);

    List<IntegrationProcess> result =
            repository.findByNameContainingIgnoreCase(
                    "sinpe"
            );

    assertFalse(result.isEmpty());

    assertTrue(
            result.stream()
                    .anyMatch(
                            p -> p.getName()
                                    .equals("SINPE Payment Service")
                    )
    );
}

@Test
void shouldFindByStatusAndType() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "SINPE Service",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    repository.saveAndFlush(process);

    List<IntegrationProcess> result =
            repository.findByStatusAndType(
                    ProcessStatus.RUNNING,
                    ProcessType.OSB
            );

    assertFalse(result.isEmpty());

    assertTrue(
            result.stream()
                    .anyMatch(
                            p -> p.getName()
                                    .equals("SINPE Service")
                    )
    );
}

@Test
void shouldSupportPagination() {

    for (int i = 1; i <= 5; i++) {

        repository.save(
                new IntegrationProcess(
                        "Process " + i,
                        ProcessType.OSB,
                        ProcessStatus.RUNNING
                )
        );
    }

    repository.flush();

    Page<IntegrationProcess> result =
            repository.findAll(
                    PageRequest.of(0, 2)
            );

    assertEquals(2, result.getContent().size());
    assertTrue(result.getTotalElements() >= 5);
    assertEquals(0, result.getNumber());
    assertEquals(2, result.getSize());
}

@Test
void shouldDeleteProcess() {

    IntegrationProcess process =
            new IntegrationProcess(
                    "Process To Delete",
                    ProcessType.OSB,
                    ProcessStatus.RUNNING
            );

    IntegrationProcess saved =
            repository.saveAndFlush(process);

    Long id = saved.getId();

    repository.deleteById(id);

    repository.flush();

    assertFalse(
            repository.existsById(id)
    );
}


}
