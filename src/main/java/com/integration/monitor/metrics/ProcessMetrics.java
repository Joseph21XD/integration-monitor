package com.integration.monitor.metrics;

import org.springframework.stereotype.Component;

import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.repository.IntegrationProcessRepository;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class ProcessMetrics {

    private final MeterRegistry meterRegistry;
    private final IntegrationProcessRepository repository;

    public ProcessMetrics(
            IntegrationProcessRepository repository,
            MeterRegistry meterRegistry) {

        this.repository = repository;
        this.meterRegistry = meterRegistry;

        for (ProcessStatus status : ProcessStatus.values()) {

            Gauge.builder(
                    "integration.processes.active",
                    repository,
                    repo -> repo.findByStatus(status).size()
            )
                    .description("Current number of integration processes by status")
                    .tag("status", status.name())
                    .register(meterRegistry);
        }
    }

    public void incrementProcessStatus(ProcessStatus status) {

        meterRegistry.counter(
                "integration.processes",
                "status",
                status.name()
        ).increment();
    }

    public void incrementExecutionStarted() {

        meterRegistry.counter(
                "integration.executions",
                "status",
                "STARTED"
        ).increment();
    }

    public void incrementExecutionCompleted() {

        meterRegistry.counter(
                "integration.executions",
                "status",
                "SUCCESS"
        ).increment();
    }

    public void incrementExecutionFailed() {

        meterRegistry.counter(
                "integration.executions",
                "status",
                "FAILED"
        ).increment();
    }
}
