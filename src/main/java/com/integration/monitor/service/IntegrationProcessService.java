package com.integration.monitor.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.integration.monitor.exception.InvalidProcessException;
import com.integration.monitor.exception.ProcessNotFoundException;
import com.integration.monitor.metrics.ProcessMetrics;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.repository.IntegrationProcessRepository;
import com.integration.monitor.repository.ProcessExecutionRepository;
import com.integration.monitor.repository.specification.ProcessExecutionSpecifications;

@Service
public class IntegrationProcessService {

    private final IntegrationProcessRepository repository;
    private final ProcessExecutionRepository executionRepository;
    private final ProcessMetrics processMetrics;

    private static final Logger log
            = LoggerFactory.getLogger(IntegrationProcessService.class);

    public IntegrationProcessService(
            IntegrationProcessRepository repository,
            ProcessExecutionRepository executionRepository,
            ProcessMetrics processMetrics) {

        this.repository = repository;
        this.executionRepository = executionRepository;
        this.processMetrics = processMetrics;
    }

    @Transactional
    public IntegrationProcess registerProcess(
            IntegrationProcess process) {

        if (process == null) {
            throw new InvalidProcessException(
                    "Process cannot be null");
        }

        log.info(
                "Registering integration process: name={}, type={}, status={}",
                process.getName(),
                process.getType(),
                process.getStatus()
        );

        if (process.getName() == null
                || process.getName().isBlank()) {

            throw new InvalidProcessException(
                    "Process name is required");
        }

        if (process.getType() == null) {
            throw new InvalidProcessException(
                    "Process type is required");
        }

        if (process.getStatus() == null) {
            throw new InvalidProcessException(
                    "Process status is required");
        }

        IntegrationProcess saved = repository.save(process);

        processMetrics.incrementProcessStatus(
                process.getStatus()
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<IntegrationProcess> getAllProcesses() {
        log.info("Retrieving all integration processes");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<IntegrationProcess> getAllProcesses(
            Pageable pageable) {
        log.info("Retrieving all integration processes with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<IntegrationProcess> getProcessById(Long id) {
        log.info("Retrieving integration process by ID: {}", id);
        return repository.findById(id);
    }

    @Transactional
    public void deleteProcess(Long id) {

        log.info("Deleting integration process by ID: {}", id);

        if (!repository.existsById(id)) {
            throw new ProcessNotFoundException(id);
        }

        repository.deleteById(id);
    }

    @Transactional
    public IntegrationProcess changeStatus(
            Long id,
            ProcessStatus newStatus) {
        log.info("Changing status of integration process ID {} to {}", id, newStatus);

        IntegrationProcess process
                = repository.findById(id)
                        .orElseThrow(
                                () -> new ProcessNotFoundException(id)
                        );

        if (newStatus == null) {
            throw new InvalidProcessException(
                    "Process status cannot be null"
            );
        }

        process.changeStatus(newStatus);

        return repository.save(process);
    }

    @Transactional(readOnly = true)
    public List<IntegrationProcess> search(
            ProcessStatus status,
            ProcessType type,
            String name) {

        log.info("Searching integration processes with status={}, type={}, name={}", status, type, name);

        return repository.search(status, type, name);
    }

    @Transactional(readOnly = true)
    public List<ProcessExecution> getExecutions(Long processId) {

        log.info("Retrieving executions for integration process ID: {}", processId);

        if (!repository.existsById(processId)) {
            throw new ProcessNotFoundException(processId);
        }

        Specification<ProcessExecution> specification
                = Specification
                        .where(ProcessExecutionSpecifications.hasProcessId(processId));

        return executionRepository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "startedAt"));
    }
}
