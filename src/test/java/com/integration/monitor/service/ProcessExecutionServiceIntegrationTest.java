package com.integration.monitor.service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.integration.monitor.exception.ExecutionProcessMismatchException;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.repository.IntegrationProcessRepository;
import com.integration.monitor.repository.ProcessExecutionRepository;

@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE)
class ProcessExecutionServiceIntegrationTest {

    @Autowired
    private ProcessExecutionService service;

    @Autowired
    private IntegrationProcessRepository processRepository;

    @Autowired
    private ProcessExecutionRepository executionRepository;

    @Test
    void shouldStartExecutionAndUpdateProcessStatus() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "Integration Test",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING);

        process = processRepository.save(process);

        ProcessExecution execution
                = service.startExecution(process.getId());

        IntegrationProcess updatedProcess
                = processRepository
                        .findById(process.getId())
                        .orElseThrow();

        assertEquals(
                ProcessStatus.RUNNING,
                updatedProcess.getStatus());

        assertEquals(
                ProcessStatus.RUNNING,
                execution.getStatus());

        assertEquals(
                process.getId(),
                execution.getProcess().getId());
    }

    @Test
    void shouldRollbackWhenExecutionDoesNotBelongToProcess() {

        IntegrationProcess process1
                = new IntegrationProcess(
                        "Process 1",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING);

        IntegrationProcess process2
                = new IntegrationProcess(
                        "Process 2",
                        ProcessType.SOAP,
                        ProcessStatus.RUNNING);

        process1 = processRepository.save(process1);
        process2 = processRepository.save(process2);

        final Long process1Id = process1.getId();

        ProcessExecution execution
                = new ProcessExecution(
                        process2,
                        LocalDateTime.now(),
                        ProcessStatus.RUNNING);

        execution = executionRepository.save(execution);

        Long executionId = execution.getId();

        assertThrows(
                ExecutionProcessMismatchException.class,
                () -> service.completeExecution(
                        process1Id,
                        executionId));

        ProcessExecution persistedExecution
                = executionRepository
                        .findById(executionId)
                        .orElseThrow();

        IntegrationProcess persistedProcess
                = processRepository
                        .findById(process1Id)
                        .orElseThrow();

        assertEquals(
                ProcessStatus.RUNNING,
                persistedExecution.getStatus());

        assertEquals(
                ProcessStatus.RUNNING,
                persistedProcess.getStatus());
    }
}
