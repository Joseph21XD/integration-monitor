package com.integration.monitor.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.integration.monitor.dto.ExecutionSearchRequest;
import com.integration.monitor.exception.ExecutionNotFoundException;
import com.integration.monitor.exception.ExecutionProcessMismatchException;
import com.integration.monitor.exception.ProcessNotFoundException;
import com.integration.monitor.metrics.ProcessMetrics;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.repository.IntegrationProcessRepository;
import com.integration.monitor.repository.ProcessExecutionRepository;
import com.integration.monitor.repository.specification.ProcessExecutionSpecificationFactory;

@Service
public class ProcessExecutionService {

    private final IntegrationProcessRepository processRepository;
    private final ProcessExecutionRepository executionRepository;
    private final ProcessExecutionSpecificationFactory specificationFactory;
    private final ProcessMetrics processMetrics;

    public ProcessExecutionService(
            IntegrationProcessRepository processRepository,
            ProcessExecutionRepository executionRepository,
            ProcessExecutionSpecificationFactory specificationFactory,
            ProcessMetrics processMetrics) {

        this.processRepository = processRepository;
        this.executionRepository = executionRepository;
        this.specificationFactory = specificationFactory;
        this.processMetrics = processMetrics;
    }

    @Transactional
    public ProcessExecution startExecution(Long processId) {

        IntegrationProcess process
                = processRepository.findById(processId)
                        .orElseThrow(() -> new ProcessNotFoundException(processId));

        if (process.getStatus() != ProcessStatus.RUNNING) {
            process.changeStatus(ProcessStatus.RUNNING);
        }

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.RUNNING
                );

        ProcessExecution saved
                = executionRepository.save(execution);

        processMetrics.incrementExecutionStarted();

        return saved;
    }

    @Transactional
    public ProcessExecution completeExecution(Long processId, Long executionId) {

        IntegrationProcess process
                = processRepository.findById(processId)
                        .orElseThrow(() -> new ProcessNotFoundException(processId));

        ProcessExecution execution
                = executionRepository.findById(executionId)
                        .orElseThrow(()
                                -> new ExecutionNotFoundException(executionId));

        if (!execution.getProcess().getId().equals(process.getId())) {
            throw new ExecutionProcessMismatchException(
                    executionId,
                    processId);
        }

        execution.finish(ProcessStatus.SUCCESS);

        process.changeStatus(ProcessStatus.SUCCESS);

        processMetrics.incrementExecutionCompleted();

        return executionRepository.save(execution);
    }

    @Transactional
    public ProcessExecution failExecution(
            Long processId,
            Long executionId,
            String errorMessage) {

        IntegrationProcess process
                = processRepository.findById(processId)
                        .orElseThrow(() -> new ProcessNotFoundException(processId));

        ProcessExecution execution
                = executionRepository.findById(executionId)
                        .orElseThrow(()
                                -> new ExecutionNotFoundException(executionId));

        if (!execution.getProcess().getId().equals(process.getId())) {
            throw new IllegalArgumentException(
                    "Execution does not belong to process: " + processId);
        }

        execution.fail(errorMessage);

        process.changeStatus(ProcessStatus.FAILED);

        processMetrics.incrementExecutionFailed();

        return executionRepository.save(execution);
    }

    @Transactional(readOnly = true)
    public Page<ProcessExecution> getExecutions(
            Long processId,
            ExecutionSearchRequest request,
            Pageable pageable) {

        if (!processRepository.existsById(processId)) {
            throw new ProcessNotFoundException(processId);
        }

        LocalDateTime from = request.getFrom();
        LocalDateTime to = request.getTo();

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "'from' must be before 'to'");
        }

        Specification<ProcessExecution> specification
                = specificationFactory.build(processId, request);

        return executionRepository.findAll(
                specification,
                pageable);
    }
}
