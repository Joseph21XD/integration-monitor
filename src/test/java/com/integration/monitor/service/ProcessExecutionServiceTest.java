package com.integration.monitor.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.integration.monitor.dto.ExecutionSearchRequest;
import com.integration.monitor.exception.ExecutionNotFoundException;
import com.integration.monitor.exception.InvalidStatusTransitionException;
import com.integration.monitor.exception.ProcessNotFoundException;
import com.integration.monitor.metrics.ProcessMetrics;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.repository.IntegrationProcessRepository;
import com.integration.monitor.repository.ProcessExecutionRepository;
import com.integration.monitor.repository.specification.ProcessExecutionSpecificationFactory;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionServiceTest {

    @Mock
    private IntegrationProcessRepository processRepository;

    @Mock
    private ProcessExecutionRepository executionRepository;

    @Mock
    private ProcessMetrics processMetrics;

    @InjectMocks
    private ProcessExecutionService service;

    @Mock
    private ProcessExecutionSpecificationFactory specificationFactory;

    @Test
    void shouldGetExecutions() {

        Long processId = 1L;

        IntegrationProcess process
                = new IntegrationProcess(
                        "Test Process",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        Pageable pageable
                = PageRequest.of(0, 10);

        Specification<ProcessExecution> specification
                = (root, query, criteriaBuilder) -> null;

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.SUCCESS);

        Page<ProcessExecution> page
                = new PageImpl<>(List.of(execution), pageable, 1);

        when(processRepository.existsById(processId))
                .thenReturn(true);

        when(specificationFactory.build(processId, request))
                .thenReturn(specification);

        when(executionRepository.findAll(specification, pageable))
                .thenReturn(page);

        Page<ProcessExecution> result
                = service.getExecutions(
                        processId,
                        request,
                        pageable);

        assertEquals(1, result.getTotalElements());

        verify(processRepository)
                .existsById(processId);

        verify(specificationFactory)
                .build(processId, request);

        verify(executionRepository)
                .findAll(specification, pageable);
    }

    @Test
    void shouldStartExecution() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.RUNNING);

        when(processRepository.findById(1L))
                .thenReturn(Optional.of(process));

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.RUNNING);

        when(executionRepository.save(any(ProcessExecution.class)))
                .thenReturn(execution);

        ProcessExecution result = service.startExecution(1L);

        assertNotNull(result);
        assertEquals(ProcessStatus.RUNNING, result.getStatus());

        verify(executionRepository).save(any(ProcessExecution.class));
        verify(processMetrics).incrementExecutionStarted();
    }

    @Test
    void shouldCompleteExecution() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.RUNNING);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.RUNNING);

        setId(process, 1L);
        setId(execution, 100L);

        when(processRepository.findById(1L))
                .thenReturn(Optional.of(process));

        when(executionRepository.findById(100L))
                .thenReturn(Optional.of(execution));

        when(executionRepository.save(execution))
                .thenReturn(execution);

        ProcessExecution result
                = service.completeExecution(1L, 100L);

        assertEquals(ProcessStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getFinishedAt());
        assertEquals(ProcessStatus.SUCCESS, process.getStatus());

        verify(processMetrics).incrementExecutionCompleted();
        verify(executionRepository).save(execution);
    }

    @Test
    void shouldFailExecution() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.RUNNING);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.RUNNING);

        setId(process, 1L);
        setId(execution, 100L);

        when(processRepository.findById(1L))
                .thenReturn(Optional.of(process));

        when(executionRepository.findById(100L))
                .thenReturn(Optional.of(execution));

        when(executionRepository.save(execution))
                .thenReturn(execution);

        ProcessExecution result
                = service.failExecution(
                        1L,
                        100L,
                        "Connection timeout");

        assertEquals(ProcessStatus.FAILED, result.getStatus());
        assertEquals("Connection timeout", result.getErrorMessage());
        assertNotNull(result.getFinishedAt());
        assertEquals(ProcessStatus.FAILED, process.getStatus());

        verify(processMetrics).incrementExecutionFailed();
        verify(executionRepository).save(execution);
    }

    @Test
    void shouldRejectExecutionFromAnotherProcess() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.RUNNING);

        IntegrationProcess anotherProcess
                = new IntegrationProcess(
                        "Payment Service",
                        ProcessType.SOAP,
                        ProcessStatus.RUNNING);

        ProcessExecution execution
                = new ProcessExecution(
                        anotherProcess,
                        LocalDateTime.now(),
                        ProcessStatus.RUNNING);

        setId(process, 1L);
        setId(execution, 100L);

        when(processRepository.findById(1L))
                .thenReturn(Optional.of(process));

        when(executionRepository.findById(999L))
                .thenThrow(new ExecutionNotFoundException(999L));

        assertThrows(
                ExecutionNotFoundException.class,
                () -> service.completeExecution(1L, 999L)
        );

        verify(executionRepository, never())
                .save(any());

        verifyNoInteractions(processMetrics);
    }

    @Test
    void shouldNotCompleteAlreadyFinishedExecution() {

        IntegrationProcess process
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.REST_API,
                        ProcessStatus.SUCCESS);

        ProcessExecution execution
                = new ProcessExecution(
                        process,
                        LocalDateTime.now(),
                        ProcessStatus.SUCCESS);

        setId(process, 1L);
        setId(execution, 100L);

        when(processRepository.findById(1L))
                .thenReturn(Optional.of(process));

        when(executionRepository.findById(100L))
                .thenReturn(Optional.of(execution));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.completeExecution(1L, 100L));

        verify(executionRepository, never())
                .save(any());

        verifyNoInteractions(processMetrics);
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldThrowExceptionWhenProcessDoesNotExist() {

        Long processId = 999L;

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        Pageable pageable
                = PageRequest.of(0, 10);

        when(processRepository.existsById(processId))
                .thenReturn(false);

        assertThrows(
                ProcessNotFoundException.class,
                () -> service.getExecutions(
                        processId,
                        request,
                        pageable));

        verify(processRepository)
                .existsById(processId);

        verifyNoInteractions(specificationFactory);
        verifyNoInteractions(executionRepository);
    }

    @Test
    void shouldRejectInvalidDateRange() {

        Long processId = 1L;

        ExecutionSearchRequest request
                = new ExecutionSearchRequest();

        request.setFrom(
                LocalDateTime.of(2026, 9, 12, 20, 0));

        request.setTo(
                LocalDateTime.of(2026, 9, 12, 19, 0));

        Pageable pageable
                = PageRequest.of(0, 10);

        when(processRepository.existsById(processId))
                .thenReturn(true);

        IllegalArgumentException exception
                = assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getExecutions(
                                processId,
                                request,
                                pageable));

        assertEquals(
                "'from' must be before 'to'",
                exception.getMessage());

        verify(processRepository)
                .existsById(processId);

        verifyNoInteractions(specificationFactory);
        verifyNoInteractions(executionRepository);
    }
}
