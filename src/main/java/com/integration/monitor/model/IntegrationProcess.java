package com.integration.monitor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.integration.monitor.exception.InvalidStatusTransitionException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "INTEGRATION_PROCESS")
public class IntegrationProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private ProcessType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ProcessStatus status;

    @CreationTimestamp
    @Column(
            name = "CREATED_AT",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "UPDATED_AT",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "process",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProcessExecution> executions
            = new ArrayList<>();

    protected IntegrationProcess() {
    }

    public IntegrationProcess(
            String name,
            ProcessType type,
            ProcessStatus status) {

        this.name = name;
        this.type = type;
        this.status = status;
    }

    public IntegrationProcess(
            Long id,
            String name,
            ProcessType type,
            ProcessStatus status) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProcessType getType() {
        return type;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void addExecution(ProcessExecution execution) {
        executions.add(execution);
        execution.setProcess(this);
    }

    public void changeStatus(ProcessStatus newStatus) {

        if (newStatus == null) {
            throw new InvalidStatusTransitionException(
                    "Status cannot be null"
            );
        }

        if (this.status == ProcessStatus.RUNNING) {

            if (newStatus == ProcessStatus.SUCCESS
                    || newStatus == ProcessStatus.FAILED
                    || newStatus == ProcessStatus.STOPPED) {

                this.status = newStatus;
                return;
            }
        }

        throw new InvalidStatusTransitionException(
                "Cannot change status from "
                + this.status
                + " to "
                + newStatus
        );
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "IntegrationProcess{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", type='" + type + '\''
                + ", status=" + status
                + '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof IntegrationProcess)) {
            return false;
        }

        IntegrationProcess that
                = (IntegrationProcess) o;

        return id != null
                && id.equals(that.id);
    }

    @Override
    public int hashCode() {

        return id != null
                ? id.hashCode()
                : 0;
    }
}
