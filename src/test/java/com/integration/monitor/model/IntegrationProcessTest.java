package com.integration.monitor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.integration.monitor.exception.InvalidStatusTransitionException;

class IntegrationProcessTest {

    @Test
    void shouldAllowRunningToSuccess() {

        IntegrationProcess process =
                new IntegrationProcess(
                        "Test",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING);

        process.changeStatus(ProcessStatus.SUCCESS);

        assertEquals(
                ProcessStatus.SUCCESS,
                process.getStatus());
    }

    @Test
    void shouldAllowRunningToFailed() {

        IntegrationProcess process =
                new IntegrationProcess(
                        "Test",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING);

        process.changeStatus(ProcessStatus.FAILED);

        assertEquals(
                ProcessStatus.FAILED,
                process.getStatus());
    }

    @Test
    void shouldAllowRunningToStopped() {

        IntegrationProcess process =
                new IntegrationProcess(
                        "Test",
                        ProcessType.OSB,
                        ProcessStatus.RUNNING);

        process.changeStatus(ProcessStatus.STOPPED);

        assertEquals(
                ProcessStatus.STOPPED,
                process.getStatus());
    }

    @Test
    void shouldRejectSuccessToFailed() {

        IntegrationProcess process =
                new IntegrationProcess(
                        "Test",
                        ProcessType.OSB,
                        ProcessStatus.SUCCESS);

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> process.changeStatus(ProcessStatus.FAILED));
    }
}