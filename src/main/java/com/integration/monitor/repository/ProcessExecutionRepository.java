package com.integration.monitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.integration.monitor.model.ProcessExecution;

@Repository
public interface ProcessExecutionRepository
        extends JpaRepository<ProcessExecution, Long>,
                JpaSpecificationExecutor<ProcessExecution> {

}
