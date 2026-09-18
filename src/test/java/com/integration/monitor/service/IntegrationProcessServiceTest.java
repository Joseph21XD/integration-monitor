package com.integration.monitor.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
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

import com.integration.monitor.exception.InvalidProcessException;
import com.integration.monitor.exception.InvalidStatusTransitionException;
import com.integration.monitor.exception.ProcessNotFoundException;
import com.integration.monitor.metrics.ProcessMetrics;
import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;
import com.integration.monitor.repository.IntegrationProcessRepository;

@ExtendWith(MockitoExtension.class)
class IntegrationProcessServiceTest {

    @Mock
    private IntegrationProcessRepository repository;

    @Mock
    private ProcessMetrics processMetrics;

    @InjectMocks
    private IntegrationProcessService service;

    private IntegrationProcess process;

    @BeforeEach
    void setUp() {
        process = new IntegrationProcess(
                "SINPE Service",
                ProcessType.OSB,
                ProcessStatus.RUNNING
        );
    }

    @Test
    void shouldRegisterProcess() {

        when(repository.save(process))
                .thenReturn(process);

        IntegrationProcess result
                = service.registerProcess(process);

        assertNotNull(result);
        assertEquals("SINPE Service", result.getName());
        assertEquals(ProcessType.OSB, result.getType());
        assertEquals(ProcessStatus.RUNNING, result.getStatus());

        verify(repository).save(process);

        verify(processMetrics)
                .incrementProcessStatus(ProcessStatus.RUNNING);
    }

    @Test
    void shouldRejectNullProcess() {

        assertThrows(
                InvalidProcessException.class,
                () -> service.registerProcess(null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectBlankName() {

        IntegrationProcess invalidProcess
                = new IntegrationProcess(
                        "   ",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING
                );

        assertThrows(
                InvalidProcessException.class,
                () -> service.registerProcess(invalidProcess)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullType() {

        IntegrationProcess invalidProcess
                = new IntegrationProcess(
                        "SINPE Service",
                        null,
                        ProcessStatus.RUNNING
                );

        assertThrows(
                InvalidProcessException.class,
                () -> service.registerProcess(invalidProcess)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullStatus() {

        IntegrationProcess invalidProcess
                = new IntegrationProcess(
                        "SINPE Service",
                        ProcessType.OSB,
                        null
                );

        assertThrows(
                InvalidProcessException.class,
                () -> service.registerProcess(invalidProcess)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturnAllProcesses() {

        IntegrationProcess process2
                = new IntegrationProcess(
                        "Payment API",
                        ProcessType.REST_API,
                        ProcessStatus.SUCCESS
                );

        when(repository.findAll())
                .thenReturn(List.of(process, process2));

        List<IntegrationProcess> result
                = service.getAllProcesses();

        assertEquals(2, result.size());
        assertEquals("SINPE Service", result.get(0).getName());
        assertEquals("Payment API", result.get(1).getName());

        verify(repository).findAll();
    }

    @Test
    void shouldReturnProcessById() {

        Long id = 1L;

        when(repository.findById(id))
                .thenReturn(Optional.of(process));

        Optional<IntegrationProcess> result
                = service.getProcessById(id);

        assertTrue(result.isPresent());
        assertEquals(process, result.get());

        verify(repository).findById(id);
    }

    @Test
    void shouldReturnEmptyWhenProcessDoesNotExist() {

        Long id = 999L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        Optional<IntegrationProcess> result
                = service.getProcessById(id);

        assertTrue(result.isEmpty());

        verify(repository).findById(id);
    }

    @Test
    void shouldChangeStatus() {

        Long id = 1L;

        when(repository.findById(id))
                .thenReturn(Optional.of(process));

        when(repository.save(process))
                .thenReturn(process);

        IntegrationProcess result
                = service.changeStatus(
                        id,
                        ProcessStatus.SUCCESS
                );

        assertEquals(
                ProcessStatus.SUCCESS,
                result.getStatus()
        );

        verify(repository).findById(id);
        verify(repository).save(process);
    }

    @Test
    void shouldThrowExceptionWhenChangingStatusOfNonExistingProcess() {

        Long id = 999L;

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ProcessNotFoundException.class,
                () -> service.changeStatus(
                        id,
                        ProcessStatus.SUCCESS
                )
        );

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {

        Long id = 1L;

        process.changeStatus(ProcessStatus.SUCCESS);

        when(repository.findById(id))
                .thenReturn(Optional.of(process));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> service.changeStatus(
                        id,
                        ProcessStatus.RUNNING
                )
        );

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteProcess() {

        Long id = 1L;

        when(repository.existsById(id))
                .thenReturn(true);

        service.deleteProcess(id);

        verify(repository).existsById(id);
        verify(repository).deleteById(id);
    }

}
