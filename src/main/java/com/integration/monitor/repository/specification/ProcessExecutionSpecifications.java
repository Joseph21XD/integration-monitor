package com.integration.monitor.repository.specification;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.integration.monitor.model.ProcessExecution;
import com.integration.monitor.model.ProcessStatus;

public final class ProcessExecutionSpecifications {

    private ProcessExecutionSpecifications() {
    }

    public static Specification<ProcessExecution> hasProcessId(
            Long processId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("process").get("id"),
                        processId);
    }

    public static Specification<ProcessExecution> hasStatus(
            ProcessStatus status) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status);
    }

    public static Specification<ProcessExecution> startedAtAfterOrEqual(
            LocalDateTime from) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("startedAt"),
                        from);
    }

    public static Specification<ProcessExecution> startedAtBeforeOrEqual(
            LocalDateTime to) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("startedAt"),
                        to);
    }
}