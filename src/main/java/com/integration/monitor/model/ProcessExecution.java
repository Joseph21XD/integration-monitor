package com.integration.monitor.model;

import java.time.LocalDateTime;

import com.integration.monitor.exception.InvalidStatusTransitionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROCESS_EXECUTION")
public class ProcessExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    private IntegrationProcess process;

    @Column(name = "STARTED_AT", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "FINISHED_AT")
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ProcessStatus status;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    protected ProcessExecution() {
    }

    public ProcessExecution(
            IntegrationProcess process,
            LocalDateTime startedAt,
            ProcessStatus status) {

        this.process = process;
        this.startedAt = startedAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public IntegrationProcess getProcess() {
        return process;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    void setProcess(IntegrationProcess process) {
        this.process = process;
    }

    public void finish(ProcessStatus status) {

        validateRunning();

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (status != ProcessStatus.SUCCESS) {
            throw new InvalidStatusTransitionException(
                    "Execution can only be completed with SUCCESS");
        }

        this.status = status;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {

        validateRunning();

        this.status = ProcessStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    private void validateRunning() {
        if (this.status != ProcessStatus.RUNNING) {
            throw new InvalidStatusTransitionException(
                    "Execution is already finished with status: " + this.status);
        }
    }
}
