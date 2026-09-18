package com.integration.monitor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.integration.monitor.model.IntegrationProcess;
import com.integration.monitor.model.ProcessStatus;
import com.integration.monitor.model.ProcessType;

@Repository
public interface IntegrationProcessRepository
        extends JpaRepository<IntegrationProcess, Long> {

    List<IntegrationProcess> findByStatus(
            ProcessStatus status);

    List<IntegrationProcess> findByType(
            ProcessType type);

    List<IntegrationProcess> findByStatusAndType(
            ProcessStatus status,
            ProcessType type);

    List<IntegrationProcess> findByNameContainingIgnoreCase(
            String name);

    @Query("""
    SELECT p
    FROM IntegrationProcess p
    WHERE (:status IS NULL OR p.status = :status)
      AND (:type IS NULL OR p.type = :type)
      AND (:name IS NULL OR
           LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
""")
    List<IntegrationProcess> search(
            @Param("status") ProcessStatus status,
            @Param("type") ProcessType type,
            @Param("name") String name);

    @Query("""
    SELECT DISTINCT p
    FROM IntegrationProcess p
    LEFT JOIN FETCH p.executions
    WHERE p.id = :id
""")
    Optional<IntegrationProcess> findByIdWithExecutions(
            @Param("id") Long id);
}
