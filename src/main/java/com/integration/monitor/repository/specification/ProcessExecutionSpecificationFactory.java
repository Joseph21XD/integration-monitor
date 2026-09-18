package com.integration.monitor.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.integration.monitor.dto.ExecutionSearchRequest;
import com.integration.monitor.model.ProcessExecution;

@Component
public class ProcessExecutionSpecificationFactory {

    public Specification<ProcessExecution> build(
            Long processId,
            ExecutionSearchRequest request) {

        Specification<ProcessExecution> specification =
                Specification
                        .where(ProcessExecutionSpecifications
                                .hasProcessId(processId));

        if (request.getStatus() != null) {
            specification = specification.and(
                    ProcessExecutionSpecifications
                            .hasStatus(request.getStatus()));
        }

        if (request.getFrom() != null) {
            specification = specification.and(
                    ProcessExecutionSpecifications
                            .startedAtAfterOrEqual(request.getFrom()));
        }

        if (request.getTo() != null) {
            specification = specification.and(
                    ProcessExecutionSpecifications
                            .startedAtBeforeOrEqual(request.getTo()));
        }

        return specification;
    }
}