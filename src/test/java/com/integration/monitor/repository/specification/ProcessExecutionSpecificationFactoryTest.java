package com.integration.monitor.repository.specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import com.integration.monitor.dto.ExecutionSearchRequest;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.repository.IntegrationProcessRepository;
import com.integration.monitor.repository.ProcessExecutionRepository;

@ActiveProfiles("dev")
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProcessExecutionSpecificationFactoryTest {

    @Autowired
    private IntegrationProcessRepository processRepository;

    @Autowired
    private ProcessExecutionRepository executionRepository;

    private final ProcessExecutionSpecificationFactory factory
            = new ProcessExecutionSpecificationFactory();

    @Test
    void shouldFilterByProcessId() {

        IntegrationProcess process1
                = new IntegrationProcess(
                        "Process 1",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        IntegrationProcess process2
                = new IntegrationProcess(
                        "Process 2",
                        ProcessType.SOAP,
                        ProcessStatus.SUCCESS);

        processRepository.saveAll(List.of(process1, process2));

        executionRepository.save(
                new ProcessExecution(
                        process1,
                        LocalDateTime.now(),
                        ProcessStatus.SUCCESS));

        executionRepository.save(
                new ProcessExecution(
                        process2,
                        LocalDateTime.now(),
                        ProcessStatus.SUCCESS));

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        Specification<ProcessExecution> specification
                = factory.build(process1.getId(), request);

        var result
                = executionRepository.findAll(
                        specification,
                        PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(
                process1.getId(),
                result.getContent().get(0).getProcess().getId());
    }

    @Test
    void shouldFilterByProcessIdStatusAndDateRange() {

        LocalDateTime now = LocalDateTime.now();

        IntegrationProcess process
                = new IntegrationProcess(
                        "Process 1",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        processRepository.save(process);

        ProcessExecution matchingExecution
                = new ProcessExecution(
                        process,
                        now.minusHours(2),
                        ProcessStatus.FAILED);

        ProcessExecution wrongStatus
                = new ProcessExecution(
                        process,
                        now.minusHours(1),
                        ProcessStatus.SUCCESS);

        ProcessExecution outsideDateRange
                = new ProcessExecution(
                        process,
                        now.minusDays(2),
                        ProcessStatus.FAILED);

        executionRepository.saveAll(
                List.of(
                        matchingExecution,
                        wrongStatus,
                        outsideDateRange));

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        request.setStatus(ProcessStatus.FAILED);
        request.setFrom(now.minusHours(3));
        request.setTo(now.minusHours(1));

        Specification<ProcessExecution> specification
                = factory.build(process.getId(), request);

        var result
                = executionRepository.findAll(
                        specification,
                        PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());

        ProcessExecution execution
                = result.getContent().get(0);

        assertEquals(
                ProcessStatus.FAILED,
                execution.getStatus());

        assertEquals(
                process.getId(),
                execution.getProcess().getId());

        assertEquals(
                matchingExecution.getId(),
                execution.getId());
    }

    @Test
    void shouldFilterByStatusOnly() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "Process Status",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        processRepository.save(process);

        executionRepository.save(
                new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.FAILED));

        executionRepository.save(
                new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.SUCCESS));

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        request.setStatus(ProcessStatus.FAILED);

        Specification<ProcessExecution> specification
                = factory.build(process.getId(), request);

        var result
                = executionRepository.findAll(
                        specification,
                        PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(
                ProcessStatus.FAILED,
                result.getContent().get(0).getStatus());
    }

    @Test
    void shouldFilterByDateRangeOnly() {

        LocalDateTime now = LocalDateTime.now();

        IntegrationProcess process
                = new IntegrationProcess(
                        "Process Dates",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        processRepository.save(process);

        ProcessExecution insideRange
                = new ProcessExecution(
                        process,
                        now.minusHours(2),
                        ProcessStatus.SUCCESS);

        ProcessExecution outsideRange
                = new ProcessExecution(
                        process,
                        now.minusDays(2),
                        ProcessStatus.SUCCESS);

        executionRepository.saveAll(
                List.of(insideRange, outsideRange));

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        request.setFrom(now.minusHours(3));
        request.setTo(now.minusHours(1));

        Specification<ProcessExecution> specification
                = factory.build(process.getId(), request);

        var result
                = executionRepository.findAll(
                        specification,
                        PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(
                insideRange.getId(),
                result.getContent().get(0).getId());
    }

    @Test
    void shouldReturnAllExecutionsWhenNoOptionalFiltersAreProvided() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "Process All",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        processRepository.save(process);

        executionRepository.saveAll(
                List.of(
                        new ProcessExecution(
                                process,
                                LocalDateTime.now().minusHours(2),
                                ProcessStatus.SUCCESS),
                        new ProcessExecution(
                                process,
                                LocalDateTime.now().minusHours(1),
                                ProcessStatus.FAILED),
                        new ProcessExecution(
                                process,
                                LocalDateTime.now(),
                                ProcessStatus.RUNNING)
                ));

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        Specification<ProcessExecution> specification
                = factory.build(process.getId(), request);

        var result
                = executionRepository.findAll(
                        specification,
                        PageRequest.of(0, 10));

        assertEquals(3, result.getTotalElements());
    }
}
